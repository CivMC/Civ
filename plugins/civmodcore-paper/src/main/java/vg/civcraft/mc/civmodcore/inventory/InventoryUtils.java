package vg.civcraft.mc.civmodcore.inventory;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

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
	 *
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
	 * <p>Attempts to find the first safe place to put an item.</p>
	 *
	 * @param inventory The inventory to attempt to find a slot in.
	 * @param item The item to find a place for.
	 * @return Returns an index of a slot that it's safe to add to. A return value of -1 means no safe place. Even if
	 *         the return value is -1 it may still be <i>possible</i> to add the item stack to the inventory, as this
	 *         function attempts to find the first slot that the given item stack can fit into wholly; that if it can
	 *         technically fit but has to be distributed then there's no "first empty".
	 */
	public static int firstEmpty(@Nullable final Inventory inventory, final ItemStack item) {
		if (inventory == null) {
			return -1;
		}
		// If there's a slot free, then just return that. Otherwise, if
		// the item is invalid, just return whatever slot was returned.
		final int index = inventory.firstEmpty();
		if (index >= 0 || !ItemUtils.isValidItem(item)) {
			return index;
		}
		// If gets here, then we're certain that there's no stacks free.
		// If the amount of the item to add is larger than a stack, then
		// it can't be merged with another stack. So just back out.
		final int remainder = item.getMaxStackSize() - item.getAmount();
		if (remainder <= 0) {
			return -1;
		}
		// Find all items that match with the given item to see if there's
		// a stack that can be merged with. If none can be found, back out.
		for (final Map.Entry<Integer, ? extends ItemStack> entry : inventory.all(item).entrySet()) {
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
	 */
	public static void clearInventory(@Nonnull final Inventory inventory) {
		final ItemStack[] contents = inventory.getContents();
		Arrays.fill(contents, new ItemStack(Material.AIR));
		inventory.setContents(contents);
	}

	/**
	 * Checks whether an inventory has more than one viewer.
	 *
	 * @param inventory The inventory to check.
	 * @return Returns true if an inventory has multiple viewers.
	 */
	public static boolean hasOtherViewers(@Nullable final Inventory inventory) {
		return inventory != null && inventory.getViewers().size() > 1;
	}

	/**
	 * Checks whether a certain amount of slots would be considered a valid chest inventory.
	 *
	 * @param slots The slot amount to check.
	 * @return Returns true if the slot count is or between 1-6 multiples of 9.
	 */
	public static boolean isValidChestSize(final int slots) {
		return slots > 0
				&& slots <= 54
				&& (slots % 9) == 0;
	}

	/**
	 * Will safely add a set of items to an inventory. If not all items are added, it's not committed to the inventory.
	 *
	 * @param inventory The inventory to add the items to.
	 * @param items The items to add to the inventory.
	 * @return Returns true if the items were safely added.
	 */
	public static boolean safelyAddItemsToInventory(final Inventory inventory, final ItemStack[] items) {
		Preconditions.checkArgument(isValidInventory(inventory));
		if (ArrayUtils.isEmpty(items)) {
			return true;
		}
		final Inventory clone = ClonedInventory.cloneInventory(inventory);
		for (final ItemStack itemToAdd : items) {
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
		inventory.setContents(clone.getContents());
		return true;
	}

	/**
	 * Will safely transact a set of items from one inventory to another inventory. If not all items are transacted, the
	 * transaction is not committed.
	 *
	 * @param from The inventory to move the given items from.
	 * @param to The inventory to move the given items to.
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
		formerInventory.setContents(formerClone.getContents());
		latterInventory.setContents(latterClone.getContents());
		return true;
	}

}
