package vg.civcraft.mc.civmodcore.api;

import static vg.civcraft.mc.civmodcore.util.NullCoalescing.castOrNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.util.Iteration;

/**
 * Class of static APIs for Items. Replaces ISUtils.
 *
 * @deprecated Use {@link ItemUtils} instead.
 */
@Deprecated
public final class ItemAPI {

	/**
	 * Checks if an ItemStack is valid. An ItemStack is considered valid if when added to an inventory, it shows as an
	 * item with an amount within appropriate bounds. Therefore {@code new ItemStack(Material.AIR)} will not be
	 * considered valid, nor will {@code new ItemStack(Material.STONE, 80)}
	 *
	 * @param item The item to validate.
	 * @return Returns true if the item is valid.
	 *
	 * @deprecated Use {@link ItemUtils#isValidItem(ItemStack)} instead.
	 */
	@Deprecated
	public static boolean isValidItem(ItemStack item) {
		if (item == null) {
			return false;
		}
		if (!MaterialAPI.isValidItemMaterial(item.getType())) {
			return false;
		}
		if (!isValidItemAmount(item)) {
			return false;
		}
		return true;
	}

	/**
	 * Checks if an ItemStack has a valid amount.
	 *
	 * @param item The item to validate.
	 * @return Returns true if the item has a valid amount.
	 *
	 * @deprecated Use {@link ItemUtils#isValidItemAmount(ItemStack)} instead.
	 */
	@Deprecated
	public static boolean isValidItemAmount(ItemStack item) {
		if (item == null) {
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
	 *
	 * @deprecated Use {@link ItemUtils#areItemsEqual(ItemStack, ItemStack)} instead.
	 */
	@Deprecated
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
	 *
	 * @deprecated Use {@link ItemUtils#areItemsSimilar(ItemStack, ItemStack)} instead.
	 */
	@Deprecated
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
	 *
	 * @deprecated Use {@link ItemUtils#decrementItem(ItemStack)} instead.
	 */
	@Deprecated
	public static ItemStack decrementItem(ItemStack item) {
		if (item == null || item.getAmount() <= 1) {
			return null;
		}
		item.setAmount(item.getAmount() - 1);
		return item;
	}

	/**
	 * Normalizes an item.
	 *
	 * @param item The item to normalize.
	 * @return The normalized item.
	 *
	 * @deprecated Use {@link ItemUtils#normalizeItem(ItemStack)} instead.
	 */
	@Deprecated
	public static ItemStack normalizeItem(ItemStack item) {
		if (item == null) {
			return null;
		}
		item = item.clone();
		item.setAmount(1);
		return item;
	}

	/**
	 * Retrieves the ItemMeta from an item.
	 *
	 * @param item The item to retrieve meta from.
	 * @return Returns the item meta, which is never null.
	 *
	 * @deprecated Use {@link ItemUtils#getItemMeta(ItemStack)} instead.
	 */
	@Deprecated
	public static ItemMeta getItemMeta(ItemStack item) {
		if (item == null) {
			throw new IllegalArgumentException("Cannot retrieve the item's meta; the item is null.");
		}
		ItemMeta meta = item.getItemMeta();
		if (meta == null) {
			throw new IllegalArgumentException("Cannot retrieve item meta; it has no meta nor was any generated.");
		}
		return meta;
	}

	/**
	 * Retrieves the display name from an item.
	 *
	 * @param item The item to retrieve the display name from.
	 * @return Returns the display name of an item. Will return null if there's no display name, or if it's empty.
	 *
	 * @deprecated Use {@link ItemUtils#getDisplayName(ItemStack)} instead.
	 */
	@Deprecated
	public static String getDisplayName(ItemStack item) {
		ItemMeta meta = getItemMeta(item);
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
	 *
	 * @deprecated Use {@link ItemUtils#setDisplayName(ItemStack, String)} instead.
	 */
	@Deprecated
	public static void setDisplayName(ItemStack item, String name) {
		ItemMeta meta = getItemMeta(item);
		if (StringUtils.isEmpty(name)) {
			meta.setDisplayName(null);
		}
		else {
			meta.setDisplayName(name);
		}
		item.setItemMeta(meta);
	}

	/**
	 * Retrieves the lore from an item.
	 *
	 * @param item The item to retrieve the lore from.
	 * @return Returns the lore, which is never null.
	 *
	 * @deprecated Use {@link ItemUtils#getLore(ItemStack)} instead.
	 */
	@Deprecated
	public static List<String> getLore(ItemStack item) {
		ItemMeta meta = getItemMeta(item);
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
	 *
	 * @see ItemAPI#clearLore(ItemStack)
	 *
	 * @deprecated Use {@link ItemUtils#setLore(ItemStack, String...)} instead.
	 */
	@Deprecated
	public static void setLore(ItemStack item, String... lines) {
		if (ArrayUtils.isEmpty(lines)) {
			clearLore(item);
		}
		else {
			setLore(item, Iteration.collect(ArrayList::new, lines));
		}
	}

	/**
	 * Sets the lore for an item, replacing any lore that may have already been set.
	 *
	 * @param item The item to set the lore to.
	 * @param lines The lore to set to the item.
	 *
	 * @see ItemAPI#clearLore(ItemStack)
	 *
	 * @deprecated Use {@link ItemUtils#setLore(ItemStack, List)} instead.
	 */
	@Deprecated
	public static void setLore(ItemStack item, List<String> lines) {
		ItemMeta meta = getItemMeta(item);
		meta.setLore(lines);
		item.setItemMeta(meta);
	}

	/**
	 * Appends lore to an item.
	 *
	 * @param item The item to append the lore to.
	 * @param lines The lore to append to the item.
	 *
	 * @deprecated Use {@link ItemUtils#addLore(ItemStack, String...)} instead.
	 */
	@Deprecated
	public static void addLore(ItemStack item, String... lines) {
		addLore(item, Iteration.collect(ArrayList::new, lines));
	}

	/**
	 * Appends lore to an item.
	 *
	 * @param item The item to append the lore to.
	 * @param lines The lore to append to the item.
	 *
	 * @deprecated Use {@link ItemUtils#addLore(ItemStack, List)} instead.
	 */
	@Deprecated
	public static void addLore(ItemStack item, List<String> lines) {
		addLore(item, false, lines);
	}

	/**
	 * Adds lore to an item, either by appending or prepending.
	 *
	 * @param item The item to append the lore to.
	 * @param prepend If set to true, the lore will be prepended instead of appended.
	 * @param lines The lore to append to the item.
	 *
	 * @deprecated Use {@link ItemUtils#addLore(ItemStack, boolean, String...)} instead.
	 */
	@Deprecated
	public static void addLore(ItemStack item, boolean prepend, String... lines) {
		addLore(item, prepend, Iteration.collect(ArrayList::new, lines));
	}

	/**
	 * Adds lore to an item, either by appending or prepending.
	 *
	 * @param item The item to append the lore to.
	 * @param prepend If set to true, the lore will be prepended instead of appended.
	 * @param lines The lore to append to the item.
	 *
	 * @deprecated Use {@link ItemUtils#addLore(ItemStack, boolean, List)} instead.
	 */
	@Deprecated
	public static void addLore(ItemStack item, boolean prepend, List<String> lines) {
		if (Iteration.isNullOrEmpty(lines)) {
			throw new IllegalArgumentException("Cannot add to the item's lore; the lore is null.");
		}
		ItemMeta meta = getItemMeta(item);
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
		setLore(item, lore);
	}

	/**
	 * Clears the lore from an item.
	 *
	 * @param item The item to clear lore of.
	 *
	 * @deprecated Use {@link ItemUtils#clearLore(ItemStack)} instead.
	 */
	@Deprecated
	public static void clearLore(ItemStack item) {
		setLore(item, (List<String>) null);
	}

	/**
	 * Retrieves the Damageable ItemMeta only if it's relevant to the item. This is necessary because [almost?] every
	 * ItemMeta implements Damageable.. for some reason. And so this will only return a Damageable instance if the item
	 * material actually has a maximum durability above zero.
	 *
	 * @param item The item to get the Damageable meta from.
	 * @return Returns an instance of Damageable, or null.
	 *
	 * @deprecated Use {@link ItemUtils#getDamageable(ItemStack)} instead.
	 */
	@Deprecated
	public static Damageable getDamageable(ItemStack item) {
		if (item == null) {
			return null;
		}
		short maxDurability = item.getType().getMaxDurability();
		if (maxDurability <= 0) {
			return null;
		}
		return castOrNull(Damageable.class, item.getItemMeta());
	}

	/**
	 * Makes an item glow by adding an enchantment and the flag for hiding enchantments,
	 * so it has the enchantment glow without an enchantment being visible. Note that this
	 * does actually apply an enchantment to an item.
	 *
	 * @param item Item to apply glow to.
	 *
	 * @deprecated Use {@link ItemUtils#addGlow(ItemStack)} instead.
	 */
	@Deprecated
	public static void addGlow(ItemStack item) {
		ItemMeta meta = getItemMeta(item);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
		item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
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
	 *
	 * @deprecated Use {@link ItemUtils#handleItemMeta(ItemStack, Predicate)} instead.
	 */
	@Deprecated
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

}
