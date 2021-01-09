package vg.civcraft.mc.civmodcore.inventory.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Class of static utilities for when you already have an instance of {@link ItemMeta}, such as
 * inside of {@link ItemUtils#handleItemMeta(ItemStack, Predicate)}'s handler, thus all of the
 * methods defined below will assume the presence of a valid meta instance.
 */
public final class MetaUtils {

	/**
	 * Retrieves the lore from a given item meta.
	 *
	 * @param meta The item meta to retrieve the lore from.
	 * @return Returns the lore, which is never null.
	 */
	@Nonnull
	public static List<String> getLore(@Nonnull final ItemMeta meta) {
		final List<String> lore = meta.getLore();
		if (lore == null) {
			return new ArrayList<>();
		}
		return lore;
	}

	/**
	 * Appends lore to an item.
	 *
	 * @param meta The item to append the lore to.
	 * @param lines The lore to append to the item.
	 */
	public static void addLore(@Nonnull final ItemMeta meta, final String... lines) {
		addLore(meta, false, lines);
	}

	/**
	 * Appends lore to an item.
	 *
	 * @param meta The item to append the lore to.
	 * @param lines The lore to append to the item.
	 */
	public static void addLore(@Nonnull final ItemMeta meta, final List<String> lines) {
		addLore(meta, false, lines);
	}

	/**
	 * Adds lore to an item, either by appending or prepending.
	 *
	 * @param meta The item to append the lore to.
	 * @param prepend If set to true, the lore will be prepended instead of appended.
	 * @param lines The lore to append to the item.
	 */
	public static void addLore(@Nonnull final ItemMeta meta, final boolean prepend, final String... lines) {
		if (ArrayUtils.isEmpty(lines)) {
			return;
		}
		final List<String> lore = getLore(meta);
		if (prepend) {
			ArrayUtils.reverse(lines);
			for (final String line : lines) {
				lore.add(0, line);
			}
		}
		else {
			CollectionUtils.addAll(lore, lines);
		}
		meta.setLore(lore);
	}

	/**
	 * Adds lore to an item, either by appending or prepending.
	 *
	 * @param meta The item to append the lore to.
	 * @param prepend If set to true, the lore will be prepended instead of appended.
	 * @param lines The lore to append to the item.
	 */
	public static void addLore(@Nonnull final ItemMeta meta, final boolean prepend, final List<String> lines) {
		if (CollectionUtils.isEmpty(lines)) {
			return;
		}
		final List<String> lore = getLore(meta);
		if (prepend) {
			Collections.reverse(lines);
			for (final String line : lines) {
				lore.add(0, line);
			}
		}
		else {
			lore.addAll(lines);
		}
		meta.setLore(lore);
	}

	/**
	 * Clears the lore from an item.
	 *
	 * @param meta The item meta to clear the lore of.
	 */
	public static void clearLore(@Nonnull final ItemMeta meta) {
		meta.setLore(null);
	}

	/**
	 * Makes an item glow by adding an enchantment and the flag for hiding enchantments,
	 * so it has the enchantment glow without an enchantment being visible. Note that this
	 * does actually apply an enchantment to an item.
	 *
	 * @param meta Item meta to apply glow to.
	 */
	public static void addGlow(@Nonnull final ItemMeta meta) {
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.addEnchant(Enchantment.DURABILITY, 1, true); // true = ignoreLevelRestriction
	}

}
