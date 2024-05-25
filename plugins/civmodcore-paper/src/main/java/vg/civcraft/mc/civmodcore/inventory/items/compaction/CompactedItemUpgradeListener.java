package vg.civcraft.mc.civmodcore.inventory.items.compaction;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class CompactedItemUpgradeListener implements Listener {
	@EventHandler(
		priority = EventPriority.LOWEST,
		ignoreCancelled = true
	)
	private void upgradeOnInventoryOpen(
		final @NotNull InventoryOpenEvent event
	) {
		final Inventory inventory = event.getInventory();
		if (inventory.getHolder() == null) {
			return; // GUI
		}

		for (final ItemStack item : inventory) {
			Compaction.attemptUpgrade(item);
		}
	}

	@EventHandler(
		priority = EventPriority.LOWEST,
		ignoreCancelled = true
	)
	private void upgradeOnPlayerJoin(
		final @NotNull PlayerJoinEvent event
	) {
		final Player player = event.getPlayer();
		if (player.hasPermission("cmc.debug")) {
			// Do not auto-upgrade items if they have this debug permission as there may be reasons why they are
			// carrying legacy compacted items.
			return;
		}
		for (final ItemStack item : player.getInventory()) {
			Compaction.attemptUpgrade(item);
		}
	}
}
