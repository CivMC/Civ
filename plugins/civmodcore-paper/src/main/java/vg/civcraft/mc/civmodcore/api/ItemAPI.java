package vg.civcraft.mc.civmodcore.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.util.Iteration;

/**
 * Class of static APIs for Items. Replaces ISUtils.
 */
public final class ItemAPI {

	/**
	 * Checks if an ItemStack is valid. An ItemStack is considered valid if when added to an inventory, it shows as an
	 * item with an amount within appropriate bounds. Therefore {@code new ItemStack(Material.AIR)} will not be
	 * considered valid, nor will {@code new ItemStack(Material.STONE, 80)}
	 *
	 * @param item The item to validate.
	 * @return Returns true if the item is valid.
	 */
	public static boolean isValidItem(ItemStack item) {
		if (item == null) {
			return false;
		}
		if (item.getType() == Material.AIR) {
			return false;
		}
		if (!item.getType().isItem()) {
			return false;
		}
		if (item.getAmount() <= 0) {
			return false;
		}
		if (item.getAmount() > item.getMaxStackSize()) {
			return false;
		}
		return true;
	}

	/**
	 * Determines whether two item stacks are functionally identical. (Will check both items against the other)
	 *
	 * @param former The first item.
	 * @param latter The second item.
	 * @return Returns true if both items are equal and not null.
	 *
	 * @see ItemStack#equals(Object)
	 */
	public static boolean areItemsEqual(ItemStack former, ItemStack latter) {
		if (former != null && former.equals(latter)) {
			return true;
		}
		if (latter != null && latter.equals(former)) {
			return true;
		}
		return false;
	}

	/**
	 * Determines whether two item stacks are similar. (Will check both items against the other)
	 *
	 * @param former The first item.
	 * @param latter The second item.
	 * @return Returns true if both items are similar and not null.
	 *
	 * @see ItemStack#isSimilar(ItemStack)
	 */
	public static boolean areItemsSimilar(ItemStack former, ItemStack latter) {
		if (former != null && former.isSimilar(latter)) {
			return true;
		}
		if (latter != null && latter.isSimilar(former)) {
			return true;
		}
		return false;
	}

	/**
	 * Decrements an item's amount, or returns null if the amount reaches zero.
	 *
	 * @param item The item to decrement in amount.
	 * @return Returns the given item with a decremented amount, or null.
	 */
	public static ItemStack decrementItem(ItemStack item) {
		if (item == null || item.getAmount() <= 1) {
			return null;
		}
		item.setAmount(item.getAmount() - 1);
		return item;
	}

	/**
	 * Retrieves the ItemMeta from an item.
	 *
	 * @param item The item to retrieve meta from.
	 * @return Returns the item meta.
	 */
	public static ItemMeta getItemMeta(ItemStack item) {
		if (item == null) {
			return null;
		}
		return item.getItemMeta();
	}

	/**
	 * Retrieves the display name from an item.
	 *
	 * @param item The item to retrieve the display name from.
	 * @return Returns the display name of an item. Will return null if there's no display name, or if it's empty.
	 */
	@Nullable
	public static String getDisplayName(ItemStack item) {
		ItemMeta meta = getItemMeta(item);
		if (meta == null) {
			return null;
		}
		String name = meta.getDisplayName();
		if (StringUtils.isEmpty(name)) {
			return null;
		}
		return name;
	}

	/**
	 * Sets a display name to an item. A null or empty name will remove the display name from the item.
	 *
	 * @param item The item to set the display name to.
	 * @param name The display name to set on the item.
	 * @return Returns true if the display name was set.
	 */
	public static boolean setDisplayName(ItemStack item, String name) {
		ItemMeta meta = getItemMeta(item);
		if (meta == null) {
			return false;
		}
		if (StringUtils.isEmpty(name)) {
			meta.setDisplayName(null);
		}
		else {
			meta.setDisplayName(name);
		}
		item.setItemMeta(meta);
		return true;
	}

	/**
	 * Retrieves the lore from an item.
	 *
	 * @param item The item to retrieve the lore from.
	 * @return Returns the lore, which is never null.
	 */
	public static List<String> getLore(ItemStack item) {
		ItemMeta meta = getItemMeta(item);
		if (meta == null) {
			return new ArrayList<>();
		}
		List<String> lore = meta.getLore();
		if (lore == null) {
			return new ArrayList<>();
		}
		return lore;
	}

	/**
	 * Sets the lore for an item, replacing any lore that may have already been set.
	 *
	 * @param item The item to set the lore to.
	 * @param lines The lore to set to the item.
	 * @return Returns true if the lore was set.
	 *
	 * @see ItemAPI#clearLore(ItemStack)
	 */
	public static boolean setLore(ItemStack item, String... lines) {
		if (Iteration.isNullOrEmpty(lines)) {
			return setLore(item, (List<String>) null);
		}
		else {
			return setLore(item, Iteration.collect(ArrayList::new, lines));
		}
	}

	/**
	 * Sets the lore for an item, replacing any lore that may have already been set.
	 *
	 * @param item The item to set the lore to.
	 * @param lines The lore to set to the item.
	 * @return Returns true if the lore was set.
	 *
	 * @see ItemAPI#clearLore(ItemStack)
	 */
	public static boolean setLore(ItemStack item, List<String> lines) {
		ItemMeta meta = getItemMeta(item);
		if (meta == null) {
			return false;
		}
		meta.setLore(lines);
		item.setItemMeta(meta);
		return true;
	}

	/**
	 * Appends lore to an item.
	 *
	 * @param item The item to append the lore to.
	 * @param lines The lore to append to the item.
	 * @return Returns true if the lore was added.
	 */
	public static boolean addLore(ItemStack item, String... lines) {
		return addLore(item, Iteration.collect(ArrayList::new, lines));
	}

	/**
	 * Appends lore to an item.
	 *
	 * @param item The item to append the lore to.
	 * @param lines The lore to append to the item.
	 * @return Returns true if the lore was added.
	 */
	public static boolean addLore(ItemStack item, List<String> lines) {
		return addLore(item, false, lines);
	}

	/**
	 * Adds lore to an item, either by appending or prepending.
	 *
	 * @param item The item to append the lore to.
	 * @param prepend If set to true, the lore will be prepended instead of appended.
	 * @param lines The lore to append to the item.
	 * @return Returns true if the lore was added.
	 */
	public static boolean addLore(ItemStack item, boolean prepend, String... lines) {
		return addLore(item, prepend, Iteration.collect(ArrayList::new, lines));
	}

	/**
	 * Adds lore to an item, either by appending or prepending.
	 *
	 * @param item The item to append the lore to.
	 * @param prepend If set to true, the lore will be prepended instead of appended.
	 * @param lines The lore to append to the item.
	 * @return Returns true if the lore was added.
	 */
	public static boolean addLore(ItemStack item, boolean prepend, List<String> lines) {
		ItemMeta meta = getItemMeta(item);
		if (meta == null || Iteration.isNullOrEmpty(lines)) {
			return false;
		}
		List<String> lore = meta.getLore();
		if (lore == null) {
			lore = new ArrayList<>();
		}
		if (prepend) {
			Collections.reverse(lines);
			for (String line : lines) {
				lore.add(0, line);
			}
		}
		else {
			lore.addAll(lines);
		}
		return setLore(item, lore);
	}

	/**
	 * Clears the lore from an item.
	 *
	 * @param item The item to clear lore of.
	 * @return Returns true if the lore was cleared.
	 */
	public static boolean clearLore(ItemStack item) {
		ItemMeta meta = getItemMeta(item);
		if (meta == null) {
			return false;
		}
		return setLore(item, (List<String>) null);
	}

	/**
	 * Handles an item's metadata.
	 *
	 * @param <T> The item meta type, which might not extend ItemMeta (Damageable for example)
	 * @param item The item to handle the metadata of.
	 * @param handler The item metadata handler, which should return true if modifications were made.
	 * @return Returns true if the metadata was successfully handled.
	 *
	 * @see ItemStack#getItemMeta()
	 */
	@SuppressWarnings("unchecked")
	public static <T> boolean handleItemMeta(ItemStack item, Predicate<T> handler) {
		if (item == null || handler == null) {
			return false;
		}
		T meta;
		try {
			meta = (T) item.getItemMeta();
			if (meta == null) {
				return false;
			}
			if (handler.test(meta)) {
				return item.setItemMeta((ItemMeta) meta);
			}
		}
		catch (ClassCastException ignored) { }
		return false;
	}

	/**
	 * Makes an item glow by adding an enchantment and the flag for hiding enchantments, 
	 * so it has the enchantment glow without an enchantment being visible
	 * @param item Item to apply glow to
	 */
	public static void addGlow(ItemStack item) {
		ItemMeta im = item.getItemMeta();
		im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(im);
		item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
	}

}
