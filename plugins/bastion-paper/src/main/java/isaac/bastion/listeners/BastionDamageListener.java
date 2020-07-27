package isaac.bastion.listeners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.EnderPearl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.Permissions;
import isaac.bastion.manager.BastionBlockManager;
import isaac.bastion.manager.EnderPearlManager;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public final class BastionDamageListener implements Listener {
	private BastionBlockManager blockManager;
	private EnderPearlManager pearlManager;

	public BastionDamageListener() {
		blockManager = Bastion.getBastionManager();
		pearlManager = new EnderPearlManager();
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Set<Block> blocks = new CopyOnWriteArraySet<>();
		blocks.add(event.getBlock());
		Set<BastionBlock> blocking = blockManager.getBlockingBastionsWithoutPermission(event.getBlock().getLocation(),
				event.getPlayer().getUniqueId(), PermissionType.getPermission(Permissions.BASTION_PLACE));
		for(BastionBlock bastion : blocking) {
			if (!bastion.getType().isOnlyDirectDestruction()) {
				blockManager.erodeFromPlace(event.getPlayer(), blocking);
				event.setCancelled(true);
				if (!Bastion.getSettingManager().getIgnorePlacementMessages(event.getPlayer().getUniqueId())) {
					event.getPlayer().sendMessage(ChatColor.RED + "Bastion removed block");
				}
				return;
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onWaterFlow(BlockFromToEvent event) {
		Set<Location> blocks = new HashSet<>();
		blocks.add(event.getToBlock().getLocation());
		if (stopBlockEvent(event.getBlock().getLocation(), blocks)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onTreeGrow(StructureGrowEvent event) {
		List<Location> blocks = new ArrayList<>();
		for (BlockState state : event.getBlocks()) {
			blocks.add(state.getLocation());
		}
		if (stopBlockEvent(event.getLocation(), blocks)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPistonExtend(BlockPistonExtendEvent event) {
		handlePistonEvent(event, event.getBlocks());
	}

	@EventHandler(ignoreCancelled = true)
	public void onPistonRetract(BlockPistonRetractEvent event) {
		handlePistonEvent(event, event.getBlocks());
	}
	
	private void handlePistonEvent(BlockPistonEvent event, List<Block> blocks) {
		Set<Location> involved = new HashSet<>();
		blocks.forEach(b -> {
			involved.add(b.getLocation());
			involved.add(b.getRelative(event.getDirection()).getLocation());
		});
		involved.add(event.getBlock().getRelative(event.getDirection()).getLocation());
		if (stopBlockEvent(event.getBlock().getLocation(), involved)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBucketEmpty(PlayerBucketEmptyEvent event) {
		Set<BastionBlock> blocking = blockManager.getBlockingBastionsWithoutPermission(event.getBlock().getLocation(),
				event.getPlayer().getUniqueId(), PermissionType.getPermission(Permissions.BASTION_PLACE));
		if (!blocking.isEmpty()) {
			event.setCancelled(true);
		}
	}

	private boolean stopBlockEvent(Location source, Collection<Location> locations) {
		Set<Group> blocking = blockManager.getEnteredGroupFields(source, locations);
		return !blocking.isEmpty();
	}

	@EventHandler(ignoreCancelled = true)
	public void onDispense(BlockDispenseEvent event) {
		if (!(event.getItem().getType() == Material.WATER_BUCKET || event.getItem().getType() == Material.LAVA_BUCKET
				|| event.getItem().getType() == Material.FLINT_AND_STEEL)) {
			return;
		}
		Set<Location> blocks = new HashSet<>();
		blocks.add(event.getBlock().getRelative(((Dispenser) event.getBlock().getBlockData()).getFacing()).getLocation());
		if (stopBlockEvent(event.getBlock().getLocation(), blocks)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void handleEnderPearlLanded(PlayerTeleportEvent event) {
		if (event.getCause() != TeleportCause.ENDER_PEARL) {
			return; // Only handle enderpearl cases
		}
		if (event.getPlayer().hasPermission("Bastion.bypass")) {
			return;
		}

		Set<BastionBlock> blocking = blockManager.getBlockingBastionsWithoutPermission(event.getTo(), event.getPlayer().getUniqueId(),
				PermissionType.getPermission(Permissions.BASTION_PEARL));

		Iterator<BastionBlock> i = blocking.iterator();

		while (i.hasNext()) {
			BastionBlock bastion = i.next();
			if (bastion.getType().isOnlyDirectDestruction() || !bastion.getType().isBlockPearls()
					|| (bastion.getType().isRequireMaturity() && !bastion.isMature())) {
				i.remove();
			}
		}

		if (!blocking.isEmpty()) {
			blockManager.erodeFromTeleport(event.getPlayer(), blocking);
			event.getPlayer().sendMessage(ChatColor.RED + "Ender pearl blocked by Bastion Block");
			boolean consume = false;
			for (BastionBlock block : blocking) {
				if (block.getType().isConsumeOnBlock()) {
					consume = true;
				}
			}
			if (!consume) {
				event.getPlayer().getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
			}
			event.setCancelled(true);
			return;
		}

		blocking = blockManager.getBlockingBastionsWithoutPermission(event.getFrom(), event.getPlayer().getUniqueId(),
				PermissionType.getPermission(Permissions.BASTION_PEARL));

		i = blocking.iterator();

		while (i.hasNext()) {
			BastionBlock bastion = i.next();
			if (bastion.getType().canPearlOut() || bastion.getType().isOnlyDirectDestruction()
					|| !bastion.getType().isBlockPearls()
					|| (bastion.getType().isRequireMaturity() && !bastion.isMature())) {
				i.remove();
			}
		}

		if (!blocking.isEmpty()) {
			blockManager.erodeFromTeleport(event.getPlayer(), blocking);
			event.getPlayer().sendMessage(ChatColor.RED + "Ender pearl blocked by Bastion Block");
			boolean consume = false;
			for (BastionBlock block : blocking) {
				if (block.getType().isConsumeOnBlock()) {
					consume = true;
				}
			}
			if (!consume) {
				event.getPlayer().getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
			}
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onProjectileThrownEvent(ProjectileLaunchEvent event) {
		if (event.getEntity() instanceof EnderPearl) {
			EnderPearl pearl = (EnderPearl) event.getEntity();
			pearlManager.handlePearlLaunched(pearl);
		}
	}
}
