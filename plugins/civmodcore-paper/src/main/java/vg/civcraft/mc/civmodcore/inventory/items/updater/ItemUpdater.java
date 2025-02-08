package vg.civcraft.mc.civmodcore.inventory.items.updater;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

@FunctionalInterface
public interface ItemUpdater {
    /**
     * Updates the item however the implementer sees fit.
     *
     * @param item The item to update, which is guaranteed to be a non-empty item, as determined by a {@link ItemUtils#isEmptyItem(ItemStack)} check.
     * @return Whether the item was updated.
     */
    boolean updateItem(
        @NotNull ItemStack item
    );

    /**
     * Updates all non-empty items (as determined by {@link ItemUtils#isEmptyItem(ItemStack)}) within a given inventory.
     * @return Whether any of the items in the inventory were updated.
     */
    static boolean updateInventory(
        final @NotNull ItemUpdater updater,
        final @NotNull Inventory inventory
    ) {
        boolean updated = false;
        for (final ItemStack item : inventory) {
            if (!ItemUtils.isEmptyItem(item)) {
                updated |= updater.updateItem(item);
            }
        }
        return updated;
    }
}
