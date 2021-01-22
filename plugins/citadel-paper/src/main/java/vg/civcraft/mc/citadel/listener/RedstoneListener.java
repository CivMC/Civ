package vg.civcraft.mc.citadel.listener;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Entity;
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
import vg.civcraft.mc.civmodcore.world.WorldUtils;

public class RedstoneListener implements Listener {

	private static boolean isAuthorizedPlayerNear(Reinforcement reinforcement, double distance) {
		Location reinLocation = reinforcement.getLocation();

		if (reinLocation.getWorld() == null) {
			return false;
		}

		// distance is radius, not diameter
		double diameter = distance * 2;
		Collection<Entity> entities = reinLocation.getWorld().getNearbyEntities(reinLocation, diameter, diameter,
				diameter,
				e -> e instanceof Player && !e.isDead()
						&& reinforcement.hasPermission(e.getUniqueId(), CitadelPermissionHandler.getDoors())
						&& e.getLocation().distanceSquared(reinLocation) <= diameter * diameter);
		return !entities.isEmpty();
	}

	private double maxRedstoneDistance;

	private Map<Location, List<UUID>> authorizations;

	public RedstoneListener(double maxRedstoneDistance) {
		this.maxRedstoneDistance = maxRedstoneDistance;
		this.authorizations = new HashMap<>();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Citadel.getInstance(), () -> authorizations.clear(), 1, 1);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void pistonExtend(BlockPistonExtendEvent bpee) {
		for (Block block : bpee.getBlocks()) {
			Reinforcement reinforcement = ReinforcementLogic.getReinforcementProtecting(block);
			if (reinforcement != null) {
				bpee.setCancelled(true);
				break;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void pistonRetract(BlockPistonRetractEvent bpre) {
		for (Block block : bpre.getBlocks()) {
			Reinforcement reinforcement = ReinforcementLogic.getReinforcementProtecting(block);
			if (reinforcement != null) {
				bpre.setCancelled(true);
				break;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void pressButton(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		if (!(e.getClickedBlock().getBlockData() instanceof Switch)) {
			return;
		}
		Switch button = (Switch) (e.getClickedBlock().getBlockData());
		Block buttonBlock = e.getClickedBlock();
		Block attachedBlock;
		// needs special handling because buttons attached to ceiling and ground can be
		// turned in which case the facing direction indicates this direction
		switch (button.getAttachedFace()) {
		case CEILING:
			attachedBlock = e.getClickedBlock().getRelative(BlockFace.UP);
			break;
		case FLOOR:
			attachedBlock = e.getClickedBlock().getRelative(BlockFace.DOWN);
			break;
		case WALL:
			attachedBlock = e.getClickedBlock().getRelative(button.getFacing().getOppositeFace());
			break;
		default:
			Citadel.getInstance().getLogger().warning("Could not handle button face " + button.getAttachedFace());
			return;
		}
		// prepare all sides of button itself
		setupAdjacentDoors(e.getPlayer(), buttonBlock, button.getFacing().getOppositeFace());
		// prepare all sides of the block attached to
		setupAdjacentDoors(e.getPlayer(), attachedBlock, button.getFacing());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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
		if (blockData instanceof Door) {
			// we always store the activation for the lower half of a door
			Door door = (Door) blockData;
			if (door.getHalf() == Bisected.Half.TOP) {
				block = block.getRelative(BlockFace.DOWN);
			}
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
		for (Entry<BlockFace, Block> entry : WorldUtils.getAllBlockSidesMapped(block, true).entrySet()) {
			if (entry.getKey() == skip) {
				continue;
			}
			Block rel = entry.getValue();
			BlockData blockData = rel.getBlockData();
			if (!(blockData instanceof Openable)) {
				continue;
			}
			Location locationToSave;
			if (blockData instanceof Door) {
				Door door = (Door) blockData;
				if (door.getHalf() == Bisected.Half.TOP) {
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

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void stepPressurePlate(PlayerInteractEvent e) {
		if (e.getAction() != Action.PHYSICAL) {
			return;
		}
		Material mat = e.getClickedBlock().getType();
		if (mat != Material.STONE_PRESSURE_PLATE && mat != Material.POLISHED_BLACKSTONE_PRESSURE_PLATE &&
				mat != Material.LIGHT_WEIGHTED_PRESSURE_PLATE && mat != Material.HEAVY_WEIGHTED_PRESSURE_PLATE
				&& !Tag.WOODEN_PRESSURE_PLATES.isTagged(mat)) {
			return;
		}
		setupAdjacentDoors(e.getPlayer(), e.getClickedBlock(), BlockFace.EAST_SOUTH_EAST);
		// block below
		setupAdjacentDoors(e.getPlayer(), e.getClickedBlock().getRelative(BlockFace.DOWN), BlockFace.UP);
	}

}
