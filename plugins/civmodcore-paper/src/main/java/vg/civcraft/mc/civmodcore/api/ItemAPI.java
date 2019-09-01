package vg.civcraft.mc.civmodcore.api;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Class of static APIs for Items. Replaces ISUtils.
 * */
public final class ItemAPI {

	private ItemAPI() { } // Make the class effectively static

	/**
	 * Checks if an ItemStack is valid. An ItemStack is considered valid if when added to an inventory, it shows as an
	 * item with an amount within appropriate bounds. Therefore {@code new ItemStack(Material.AIR)} will not be
	 * considered valid, nor will {@code new ItemStack(Material.STONE, 80)}
	 *
	 * @param item The item to validate.
	 * @return Returns true if the item is valid.
	 * */
	public static boolean isValidItem(@Nullable ItemStack item) {
		if (item == null) {
			return false;
		}
		if (MaterialAPI.isAir(item.getType())) {
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
	 * Determines whether two item stacks are functionally identical.
	 *
	 * @param former The first item.
	 * @param latter The second item.
	 * @return Returns true if both items are equal and not null.
	 *
	 * @see ItemStack#equals(Object)
	 * */
	public static boolean areItemsEqual(@Nullable ItemStack former, @Nullable ItemStack latter) {
		return former != null && former.equals(latter);
	}

	/**
	 * Determines whether two item stacks are similar.
	 *
	 * @param former The first item.
	 * @param latter The second item.
	 * @return Returns true if both items are similar and not null.
	 *
	 * @see ItemStack#isSimilar(ItemStack)
	 * */
	public static boolean areItemsSimilar(@Nullable ItemStack former, @Nullable ItemStack latter) {
		return former != null && former.isSimilar(latter);
	}

	/**
	 * Retrieves the ItemMeta from an item.
	 *
	 * @param item The item to retrieve meta from.
	 * @return Returns the item meta, which is never null.
	 *
	 * @throws IllegalArgumentException If the given item stack is not null or if the item meta cannot be retrieved or
	 *                                  generated.
	 * */
	@Nonnull
	public static ItemMeta getItemMeta(@Nonnull ItemStack item) {
		Preconditions.checkNotNull(item, "Cannot retrieve the item's meta; the item is null.");
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
	 * @throws IllegalArgumentException If the given item stack is null or if the {@link ItemAPI#getItemMeta(ItemStack)
	 *                                  item's meta cannot be retrieved or generated.}
	 * */
	@Nullable
	public static String getDisplayName(@Nonnull ItemStack item) {
		Preconditions.checkNotNull(item, "Cannot retrieve the item's display name; the item is null.");
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
	 * @throws IllegalArgumentException If the given item stack is null or if the {@link ItemAPI#getItemMeta(ItemStack)
	 *                                  item's meta cannot be retrieved or generated.}
	 * */
	public static void setDisplayName(@Nonnull ItemStack item, @Nullable String name) {
		Preconditions.checkNotNull(item, "Cannot set the item's display name; the item is null.");
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
	 * @throws IllegalArgumentException If the given item stack is null or if the {@link ItemAPI#getItemMeta(ItemStack)
	 *                                  item's meta cannot be retrieved or generated.}
	 * */
	@Nonnull
	public static List<String> getLore(@Nonnull ItemStack item) {
		Preconditions.checkNotNull(item, "Cannot retrieve the item's lore; the item is null.");
		List<String> lore = getItemMeta(item).getLore();
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
	 * @throws IllegalArgumentException If the given item stack is null or if the {@link ItemAPI#getItemMeta(ItemStack)
	 *                                  item's meta cannot be retrieved or generated.}
	 *
	 * @see ItemAPI#clearLore(ItemStack)
	 * */
	public static void setLore(@Nonnull ItemStack item, @Nullable String... lines) {
		if (lines == null) {
			ItemAPI.clearLore(item);
		} else {
			setLore(item, Arrays.asList(lines));
		}
	}

	/**
	 * Sets the lore for an item, replacing any lore that may have already been set.
	 *
	 * @param item The item to set the lore to.
	 * @param lines The lore to set to the item.
	 *
	 * @throws IllegalArgumentException If the given item stack is null or if the {@link ItemAPI#getItemMeta(ItemStack)
	 *                                  item's meta cannot be retrieved or generated.}
	 *
	 * @see ItemAPI#clearLore(ItemStack)
	 * */
	public static void setLore(@Nonnull ItemStack item, @Nullable List<String> lines) {
		Preconditions.checkNotNull(item, "Cannot set the item's lore; the item is null.");
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
	 * @throws IllegalArgumentException If the given item stack or lore is null or if the {@link
	 *                                  ItemAPI#getItemMeta(ItemStack) item's meta cannot be retrieved or generated.}
	 * */
	public static void addLore(@Nonnull ItemStack item, @Nonnull String... lines) {
		addLore(item, Arrays.asList(lines));
	}

	/**
	 * Appends lore to an item.
	 *
	 * @param item The item to append the lore to.
	 * @param lines The lore to append to the item.
	 *
	 * @throws IllegalArgumentException If the given item stack or lore is null or if the {@link
	 *                                  ItemAPI#getItemMeta(ItemStack) item's meta cannot be retrieved or generated.}
	 * */
	public static void addLore(@Nonnull ItemStack item, @Nonnull List<String> lines) {
		addLore(item, false, lines);
	}

	/**
	 * Adds lore to an item, either by appending or prepending.
	 *
	 * @param item The item to append the lore to.
	 * @param prepend If set to true, the lore will be prepended instead of appended.
	 * @param lines The lore to append to the item.
	 *
	 * @throws IllegalArgumentException If the given item stack or lore is null or if the {@link
	 *                                  ItemAPI#getItemMeta(ItemStack) item's meta cannot be retrieved or generated.}
	 * */
	public static void addLore(@Nonnull ItemStack item, boolean prepend, @Nonnull String... lines) {
		addLore(item, prepend, Arrays.asList(lines));
	}

	/**
	 * Adds lore to an item, either by appending or prepending.
	 *
	 * @param item The item to append the lore to.
	 * @param prepend If set to true, the lore will be prepended instead of appended.
	 * @param lines The lore to append to the item.
	 *
	 * @throws IllegalArgumentException If the given item stack or lore is null or if the {@link
	 *                                  ItemAPI#getItemMeta(ItemStack) item's meta cannot be retrieved or generated.}
	 * */
	public static void addLore(@Nonnull ItemStack item, boolean prepend, @Nonnull List<String> lines) {
		Preconditions.checkNotNull(item, "Cannot add to the item's lore; the item is null.");
		Preconditions.checkNotNull(lines, "Cannot add to the item's lore; the lore is null.");
		List<String> lore = getLore(item);
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
	 * @throws IllegalArgumentException If the given item stack is null or if the {@link ItemAPI#getItemMeta(ItemStack)
	 *                                  item's meta cannot be retrieved or generated.}
	 * */
	public static void clearLore(@Nonnull ItemStack item) {
		Preconditions.checkNotNull(item, "Cannot clear the item's lore; the item is null.");
		setLore(item, Collections.emptyList());
	}

}
