package vg.civcraft.mc.civmodcore.api;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.ClonedInventory;
import vg.civcraft.mc.civmodcore.inventory.InventoryUtils;
import vg.civcraft.mc.civmodcore.util.Iteration;

/**
 * @deprecated Use {@link InventoryUtils} instead.
 */
@Deprecated
public final class InventoryAPI {

	/**
	 * Tests an inventory to see if it's valid.
	 *
	 * @param inventory The inventory to test.
	 * @return Returns true if it's more likely than not valid.
	 *
	 * @deprecated Use {@link InventoryUtils#isValidInventory(Inventory)} instead.
	 */
	@Deprecated
	public static boolean isValidInventory(Inventory inventory) {
		if (inventory == null) {
			return false;
		}
		if (inventory.getSize() <= 0) {
			return false;
		}
		return true;
	}

	/**
	 * Get the players viewing an inventory.
	 *
	 * @param inventory The inventory to get the viewers of.
	 * @return Returns a list of players. Returns an empty list if the inventory is null.
	 *
	 * @deprecated Use {@link InventoryUtils#isValidInventory(Inventory)} instead.
	 */
	@Deprecated
	public static List<Player> getViewingPlayers(Inventory inventory) {
		if (!isValidInventory(inventory)) {
			return new ArrayList<>();
		}
		return inventory.getViewers().stream().
				map((entity) -> EntityAPI.isPlayer(entity) ? (Player) entity : null).
				filter(Objects::nonNull).
				collect(Collectors.toCollection(ArrayList::new));
	}

	/**
	 * <p>Attempts to find the first safe place to put an item.</p>
	 *
	 * @param inventory The inventory to attempt to find a slot in.
	 * @param item The item to find a place for.
	 * @return Returns an index of a slot that it's safe to add to. A return value of -1 means no safe place. Even if
	 *     the return value is -1 it may still be <i>possible</i> to add the item stack to the inventory, as this
	 *     function attempts to find the first slot that the given item stack can fit into wholly; that if it can
	 * 	   technically fit but has to be distributed then there's no "first empty".
	 *
	 * @deprecated Use {@link InventoryUtils#firstEmpty(Inventory, ItemStack)} instead.
	 */
	@Deprecated
	public static int firstEmpty(Inventory inventory, ItemStack item) {
		if (inventory == null) {
			return -1;
		}
		// If there's a slot free, then just return that. Otherwise if
		// the item is invalid, just return whatever slot was returned.
		int index = inventory.firstEmpty();
		if (index >= 0 || !ItemAPI.isValidItem(item)) {
			return index;
		}
		// If gets here, then we're certain that there's no stacks free.
		// If the amount of the item to add is larger than a stack, then
		// it can't be merged with another stack. So just back out.
		int remainder = item.getMaxStackSize() - item.getAmount();
		if (remainder <= 0) {
			return -1;
		}
		// Find all items that match with the given item to see if there's
		// a stack that can be merged with. If none can be found, back out.
		for (Map.Entry<Integer, ? extends ItemStack> entry : inventory.all(item).entrySet()) {
			if (entry.getValue().getAmount() <= remainder) {
				return entry.getKey();
			}
		}
		return -1;
	}

	/**
	 * Clears an inventory of items.
	 *
	 * @param inventory The inventory to clear of items.
	 *
	 * @deprecated Use {@link InventoryUtils#clearInventory(Inventory)} instead.
	 */
	@Deprecated
	public static void clearInventory(Inventory inventory) {
		Preconditions.checkArgument(isValidInventory(inventory));
		inventory.setContents(Iteration.fill(inventory.getContents(), null));
	}

	/**
	 * Checks whether an inventory has more than one viewer.
	 *
	 * @param inventory The inventory to check.
	 * @return Returns true if an inventory has multiple viewers.
	 *
	 * @deprecated Use {@link InventoryUtils#hasOtherViewers(Inventory)} instead.
	 */
	@Deprecated
	public static boolean hasOtherViewers(Inventory inventory) {
		if (!isValidInventory(inventory)) {
			return false;
		}
		return inventory.getViewers().size() > 1;
	}

	/**
	 * Checks whether a certain amount of slots would be considered a valid chest inventory.
	 *
	 * @param slots The slot amount to check.
	 * @return Returns true if the slot count is or between 1-6 multiples of 9.
	 *
	 * @deprecated Use {@link InventoryUtils#isValidChestSize(int)} instead.
	 */
	@Deprecated
	public static boolean isValidChestSize(int slots) {
		if (slots <= 0 || slots > 54) {
			return false;
		}
		if ((slots % 9) > 0) {
			return false;
		}
		return true;
	}

	/**
	 * Will safely add a set of items to an inventory. If not all items are added, it's not committed to the inventory.
	 *
	 * @param inventory The inventory to add the items to.
	 * @param items The items to add to the inventory.
	 * @return Returns true if the items were safely added.
	 *
	 * @deprecated Use {@link InventoryUtils#safelyAddItemsToInventory(Inventory, ItemStack[])} instead.
	 */
	@Deprecated
	public static boolean safelyAddItemsToInventory(Inventory inventory, ItemStack[] items) {
		Preconditions.checkArgument(isValidInventory(inventory));
		if (ArrayUtils.isEmpty(items)) {
			return true;
		}
		Inventory clone = cloneInventory(inventory);
		for (ItemStack itemToAdd : items) {
			if (firstEmpty(clone, itemToAdd) < 0) {
				return false;
			}
			if (!clone.addItem(itemToAdd).isEmpty()) {
				return false;
			}
		}
		inventory.setContents(clone.getContents());
		return true;
	}

	/**
	 * Will safely remove a set of items from an inventory. If not all items are removed, it's not committed to the
	 * inventory.
	 *
	 * @param inventory The inventory to remove the items from.
	 * @param items The items to remove to the inventory.
	 * @return Returns true if the items were safely removed.
	 *
	 * @deprecated Use {@link InventoryUtils#safelyRemoveItemsFromInventory(Inventory, ItemStack[])} instead.
	 */
	@Deprecated
	public static boolean safelyRemoveItemsFromInventory(Inventory inventory, ItemStack[] items) {
		Preconditions.checkArgument(isValidInventory(inventory));
		if (ArrayUtils.isEmpty(items)) {
			return true;
		}
		Inventory clone = cloneInventory(inventory);
		for (ItemStack itemToRemove : items) {
			if (!clone.removeItem(itemToRemove).isEmpty()) {
				return false;
			}
		}
		inventory.setContents(clone.getContents());
		return true;
	}

	/**
	 * Will safely transact a set of items from one inventory to another inventory. If not all items are transacted, the
	 * transaction is not committed.
	 *
	 * @param from The inventory to move the given items from.
	 * @param items The items to transact.
	 * @param to The inventory to move the given items to.
	 * @return Returns true if the items were successfully transacted.
	 *
	 * @deprecated Use {@link InventoryUtils#safelyTransactBetweenInventories(Inventory, Inventory, ItemStack[])}
	 *     instead.
	 */
	@Deprecated
	public static boolean safelyTransactBetweenInventories(Inventory from, ItemStack[] items, Inventory to) {
		Preconditions.checkArgument(isValidInventory(from));
		Preconditions.checkArgument(isValidInventory(to));
		if (ArrayUtils.isEmpty(items)) {
			return true;
		}
		Inventory fromClone = cloneInventory(from);
		Inventory toClone = cloneInventory(to);
		if (!safelyRemoveItemsFromInventory(fromClone, items)) {
			return false;
		}
		if (!safelyAddItemsToInventory(toClone, items)) {
			return false;
		}
		from.setContents(fromClone.getContents());
		to.setContents(toClone.getContents());
		return true;
	}

	/**
	 * Will safely trade items between inventories. If not all items are traded, the trade is cancelled.
	 *
	 * @param formerInventory The first inventory.
	 * @param formerItems The items to trade from the first inventory to give to the second inventory.
	 * @param latterInventory The second inventory.
	 * @param latterItems The items to trade from the second inventory to give to the first inventory.
	 * @return Returns true if the trade succeeded.
	 *
	 * @deprecated Use
	 *     {@link InventoryUtils#safelyTradeBetweenInventories(Inventory, Inventory, ItemStack[], ItemStack[])} instead.
	 */
	@Deprecated
	public static boolean safelyTradeBetweenInventories(Inventory formerInventory, ItemStack[] formerItems,
														Inventory latterInventory, ItemStack[] latterItems) {
		Preconditions.checkArgument(isValidInventory(formerInventory));
		Preconditions.checkArgument(isValidInventory(latterInventory));
		Inventory formerClone = InventoryAPI.cloneInventory(formerInventory);
		Inventory latterClone = InventoryAPI.cloneInventory(latterInventory);
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
		formerInventory.setContents(formerClone.getContents());
		latterInventory.setContents(latterClone.getContents());
		return true;
	}

	/**
	 * @deprecated Use {@link ClonedInventory#cloneInventory(Inventory)} instead.
	 */
	@Deprecated
	public static Inventory cloneInventory(Inventory inventory) {
		return ClonedInventory.cloneInventory(inventory);
	}

}
