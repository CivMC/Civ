package vg.civcraft.mc.civmodcore.inventory.items.updater.impl;

import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.items.updater.ItemUpdater;
import vg.civcraft.mc.civmodcore.inventory.items.updater.listeners.UpdateInventoryItemsOnOpen;
import vg.civcraft.mc.civmodcore.inventory.items.updater.listeners.UpdatePlayerItemsOnJoin;

public interface DefaultItemUpdaterListeners extends UpdatePlayerItemsOnJoin, UpdateInventoryItemsOnOpen {
    static @NotNull DefaultItemUpdaterListeners wrap(
        final @NotNull ItemUpdater updater
    ) {
        return updater::updateItem;
    }
}
