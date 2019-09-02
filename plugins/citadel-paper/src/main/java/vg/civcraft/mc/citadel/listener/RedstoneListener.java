package vg.civcraft.mc.citadel.listener;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelPermissionHandler;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.model.Reinforcement;

public class RedstoneListener implements Listener {

	private static boolean isAuthorizedPlayerNear(Reinforcement reinforcement, double distance) {
		Location reinLocation = reinforcement.getLocation();
		double min_x = reinLocation.getX() - distance;
		double min_z = reinLocation.getZ() - distance;
		double max_x = reinLocation.getX() + distance;
		double max_z = reinLocation.getZ() + distance;
		boolean result = false;
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.isDead()) {
				continue;
			}
			Location playerLocation = player.getLocation();
			double player_x = playerLocation.getX();
			double player_z = playerLocation.getZ();
			// Simple bounding box check to quickly rule out Players
			// before doing the more expensive playerLocation.distance
			if (player_x < min_x || player_x > max_x || player_z < min_z || player_z > max_z) {
				continue;
			}
			if (!reinforcement.hasPermission(player, CitadelPermissionHandler.getDoors())) {
				continue;
			}
			double distanceSquared = playerLocation.distance(reinLocation);
			if (distanceSquared <= distance) {
				result = true;
				break;
			}
		}
		return result;
	}

	private double maxRedstoneDistance;

	private Map<Location, List<UUID>> authorizations;

	public RedstoneListener(double maxRedstoneDistance) {
		this.maxRedstoneDistance = maxRedstoneDistance;
		this.authorizations = new HashMap<>();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Citadel.getInstance(), () -> {
			authorizations.clear();
		}, 1L, 1L);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void pistonExtend(BlockPistonExtendEvent bpee) {
		for (Block block : bpee.getBlocks()) {
			Reinforcement reinforcement = ReinforcementLogic.getReinforcementProtecting(block);
			if (reinforcement != null) {
				bpee.setCancelled(true);
				break;
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void pistonRetract(BlockPistonRetractEvent bpre) {
		for (Block block : bpre.getBlocks()) {
			Reinforcement reinforcement = ReinforcementLogic.getReinforcementProtecting(block);
			if (reinforcement != null) {
				bpre.setCancelled(true);
				break;
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void pressButton(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		if (!(e.getClickedBlock().getBlockData() instanceof Switch)) {
			return;
		}
		Switch button = (Switch) (e.getClickedBlock().getBlockData());
		Block buttonBlock = e.getClickedBlock();
		Block attachedBlock = e.getClickedBlock().getRelative(button.getFacing().getOppositeFace());
		// prepare all sides of button itself
		setupAdjacentDoors(e.getPlayer(), buttonBlock, button.getFacing());
		// prepare all sides of the block attached to
		setupAdjacentDoors(e.getPlayer(), attachedBlock, button.getFacing().getOppositeFace());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void redstonePower(BlockRedstoneEvent bre) {
		// prevent doors from being opened by redstone
		if (bre.getNewCurrent() <= 0 || bre.getOldCurrent() > 0) {
			return;
		}
		Block block = bre.getBlock();
		BlockData blockData = block.getBlockData();
		if (!(blockData instanceof Openable)) {
			return;
		}
		Openable openable = (Openable) blockData;
		if (openable.isOpen()) {
			return;
		}
		Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(block);
		if (rein == null) {
			return;
		}
		if (rein.isInsecure()) {
			boolean playerNearby = isAuthorizedPlayerNear(rein, maxRedstoneDistance);
			if (!playerNearby) {
				bre.setNewCurrent(bre.getOldCurrent());
			}
			return;
		}
		List<UUID> playersActivating = authorizations.get(block.getLocation());
		if (playersActivating == null) {
			bre.setNewCurrent(bre.getOldCurrent());
			return;
		}
		for (UUID uuid : playersActivating) {
			if (rein.hasPermission(uuid, CitadelPermissionHandler.getDoors())) {
				// single valid perm is enough to open
				return;
			}
		}
		// noone valid found nearby, so deny
		bre.setNewCurrent(bre.getOldCurrent());
	}

	private void setupAdjacentDoors(Player player, Block block, BlockFace skip) {
		for (BlockFace face : BlockListener.ALL_SIDES) {
			if (face == skip) {
				continue;
			}
			Block rel = block.getRelative(face);
			BlockData blockData = rel.getBlockData();
			if (!(blockData instanceof Openable)) {
				continue;
			}
			Location locationToSave;
			if (blockData instanceof Door) {
				if (block.getRelative(BlockFace.UP).getType() != block.getType()) {
					// block is upper half of a door
					locationToSave = rel.getRelative(BlockFace.DOWN).getLocation();
				} else {
					// already the lower half of the door
					locationToSave = rel.getLocation();
				}
			} else {
				locationToSave = rel.getLocation();
			}
			List<UUID> existingAuths = authorizations.computeIfAbsent(locationToSave, k -> new LinkedList<>());
			existingAuths.add(player.getUniqueId());
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void stepPressurePlate(PlayerInteractEvent e) {
		if (e.getAction() != Action.PHYSICAL) {
			return;
		}
		Material mat = e.getClickedBlock().getType();
		if (mat != Material.STONE_PRESSURE_PLATE && mat != Material.LIGHT_WEIGHTED_PRESSURE_PLATE
				&& mat != Material.HEAVY_WEIGHTED_PRESSURE_PLATE && !Tag.WOODEN_PRESSURE_PLATES.isTagged(mat)) {
			return;
		}
		setupAdjacentDoors(e.getPlayer(), e.getClickedBlock(), BlockFace.EAST_SOUTH_EAST);
	}

}
