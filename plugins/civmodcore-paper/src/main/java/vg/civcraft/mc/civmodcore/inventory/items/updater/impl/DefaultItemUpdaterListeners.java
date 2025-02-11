package vg.civcraft.mc.civmodcore.inventory.items.updater.impl;

import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.items.updater.ItemUpdater;
import vg.civcraft.mc.civmodcore.inventory.items.updater.listeners.UpdateInventoryItemsOnOpen;
import vg.civcraft.mc.civmodcore.inventory.items.updater.listeners.UpdatePlayerItemsOnJoin;

public interface DefaultItemUpdaterListeners extends UpdatePlayerItemsOnJoin, UpdateInventoryItemsOnOpen {
    /**
     * Pass your {@link vg.civcraft.mc.civmodcore.inventory.items.updater.ItemUpdater} implementation into this method
     * and pass the result into {@link vg.civcraft.mc.civmodcore.ACivMod#registerListener(org.bukkit.event.Listener)}
     * (or any other event-listener registration method), and your item updater will be called when players login and
     * when inventories are opened.
     */
    static @NotNull DefaultItemUpdaterListeners wrap(
        final @NotNull ItemUpdater updater
    ) {
        return updater::updateItem;
    }
}
