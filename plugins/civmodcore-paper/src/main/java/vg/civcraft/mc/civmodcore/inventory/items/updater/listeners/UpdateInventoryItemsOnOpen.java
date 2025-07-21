package vg.civcraft.mc.civmodcore.inventory.items.updater.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.items.updater.ItemUpdater;

public interface UpdateInventoryItemsOnOpen extends ItemUpdater, Listener {
    @EventHandler(
        priority = EventPriority.LOWEST,
        ignoreCancelled = true
    )
    default void updateInventoryItemsOnOpen(
        final @NotNull InventoryOpenEvent event
    ) {
        final Inventory inventory = event.getInventory();
        if (inventory.getHolder() == null) {
            return; // GUI
        }
        ItemUpdater.updateInventory(this, inventory);
    }
}
