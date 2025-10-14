package vg.civcraft.mc.civmodcore.inventory;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class InventoryUtils {

    public static final int CHEST_1_ROW = 9;
    public static final int CHEST_2_ROWS = 9 * 2;
    public static final int CHEST_3_ROWS = 9 * 3;
    public static final int CHEST_4_ROWS = 9 * 4;
    public static final int CHEST_5_ROWS = 9 * 5;
    public static final int CHEST_6_ROWS = 9 * 6;

    /**
     * Tests an inventory to see if it's valid.
     *
     * @param inventory The inventory to test.
     * @return Returns true if it's more likely than not valid.
     */
    public static boolean isValidInventory(@Nullable final Inventory inventory) {
        return inventory != null
            && inventory.getSize() > 0;
    }

    /**
     * Get the players viewing an inventory.
     *
     * @param inventory The inventory to get the viewers of.
     * @return Returns a list of players. Returns an empty list if the inventory is null.
     * @deprecated Use Java 16's instanceof pattern matching instead.
     */
    @Deprecated
    public static List<Player> getViewingPlayers(@Nullable final Inventory inventory) {
        if (!isValidInventory(inventory)) {
            return new ArrayList<>(0);
        }
        return inventory.getViewers().stream()
            .filter(entity -> entity instanceof Player)
            .map(player -> (Player) player)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Will safely add a set of items to an inventory. If not all items are added, it's not committed to the inventory.
     *
     * @param inventory The inventory to add the items to.
     * @param items     The items to add to the inventory.
     * @return Returns true if the items were safely added.
     */
    public static boolean safelyAddItemsToInventory(final Inventory inventory, final ItemStack[] items) {
        Preconditions.checkArgument(isValidInventory(inventory));
        if (ArrayUtils.isEmpty(items)) {
            return true;
        }
        final Inventory clone = ClonedInventory.cloneInventory(inventory);
        for (final ItemStack itemToAdd : items) {
            if (!clone.addItem(itemToAdd).isEmpty()) {
                return false;
            }
        }
        inventory.setStorageContents(clone.getStorageContents());
        return true;
    }

    /**
     * Will safely remove a set of items from an inventory. If not all items are removed, it's not committed to the
     * inventory.
     *
     * @param inventory The inventory to remove the items from.
     * @param items     The items to remove to the inventory.
     * @return Returns true if the items were safely removed.
     */
    public static boolean safelyRemoveItemsFromInventory(final Inventory inventory, final ItemStack[] items) {
        Preconditions.checkArgument(isValidInventory(inventory));
        if (ArrayUtils.isEmpty(items)) {
            return true;
        }
        final Inventory clone = ClonedInventory.cloneInventory(inventory);
        for (final ItemStack itemToRemove : items) {
            if (!clone.removeItem(itemToRemove).isEmpty()) {
                return false;
            }
        }
        inventory.setStorageContents(clone.getStorageContents());
        return true;
    }

    /**
     * Will safely transact a set of items from one inventory to another inventory. If not all items are transacted, the
     * transaction is not committed.
     *
     * @param from  The inventory to move the given items from.
     * @param to    The inventory to move the given items to.
     * @param items The items to transact.
     * @return Returns true if the items were successfully transacted.
     */
    public static boolean safelyTransactBetweenInventories(final Inventory from,
                                                           final Inventory to,
                                                           final ItemStack[] items) {
        Preconditions.checkArgument(isValidInventory(from));
        Preconditions.checkArgument(isValidInventory(to));
        if (ArrayUtils.isEmpty(items)) {
            return true;
        }
        final Inventory fromClone = ClonedInventory.cloneInventory(from);
        final Inventory toClone = ClonedInventory.cloneInventory(to);
        if (!safelyRemoveItemsFromInventory(fromClone, items)) {
            return false;
        }
        if (!safelyAddItemsToInventory(toClone, items)) {
            return false;
        }
        from.setStorageContents(fromClone.getStorageContents());
        to.setStorageContents(toClone.getStorageContents());
        return true;
    }

    /**
     * Will safely trade items between inventories. If not all items are traded, the trade is cancelled.
     *
     * @param formerInventory The first inventory.
     * @param formerItems     The items to trade from the first inventory to give to the second inventory.
     * @param latterInventory The second inventory.
     * @param latterItems     The items to trade from the second inventory to give to the first inventory.
     * @return Returns true if the trade succeeded.
     */
    public static boolean safelyTradeBetweenInventories(final Inventory formerInventory,
                                                        final Inventory latterInventory,
                                                        final ItemStack[] formerItems,
                                                        final ItemStack[] latterItems) {
        Preconditions.checkArgument(isValidInventory(formerInventory));
        Preconditions.checkArgument(isValidInventory(latterInventory));
        final Inventory formerClone = ClonedInventory.cloneInventory(formerInventory);
        final Inventory latterClone = ClonedInventory.cloneInventory(latterInventory);
        if (!safelyRemoveItemsFromInventory(formerClone, formerItems)) {
            return false;
        }
        if (!safelyRemoveItemsFromInventory(latterClone, latterItems)) {
            return false;
        }
        if (!safelyAddItemsToInventory(formerClone, latterItems)) {
            return false;
        }
        if (!safelyAddItemsToInventory(latterClone, formerItems)) {
            return false;
        }
        formerInventory.setStorageContents(formerClone.getStorageContents());
        latterInventory.setStorageContents(latterClone.getStorageContents());
        return true;
    }

}
