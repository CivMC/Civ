package vg.civcraft.mc.civmodcore.inventory.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;

/**
 * Class of static utilities for when you already have an instance of {@link ItemMeta}, such as
 * inside of {@link ItemUtils#handleItemMeta(ItemStack, Predicate)}'s handler, thus all of the
 * methods defined below will assume the presence of a valid meta instance.
 */
public final class MetaUtils {

	/**
	 * Determines whether two item metas are functionally identical.
	 *
	 * @param former The first item meta.
	 * @param latter The second item meta.
	 * @return Returns true if both item metas are functionally identical.
	 */
	public static boolean areMetasEqual(final ItemMeta former, final ItemMeta latter) {
		if (former == latter) {
			return true;
		}
		if (former == null || latter == null) {
			return false;
		}
		// Create a version of the items that do not have display names or lore, since those are the pain points
		final ItemMeta fakeFormer = former.clone();
		final ItemMeta fakeLatter = latter.clone();
		fakeFormer.setDisplayName(null);
		fakeFormer.setLore(null);
		fakeLatter.setDisplayName(null);
		fakeLatter.setLore(null);
		if (!Bukkit.getItemFactory().equals(fakeFormer, fakeLatter)) {
			return false;
		}
		// And compare the display name and lore manually
		if (former.hasDisplayName() != latter.hasDisplayName()) {
			return false;
		}
		if (former.hasDisplayName()) {
			if (!ChatUtils.areComponentsEqual(
					getComponentDisplayName(former),
					getComponentDisplayName(latter))) {
				return false;
			}
		}
		if (former.hasLore() != latter.hasLore()) {
			return false;
		}
		if (former.hasLore()) {
			if (!Objects.equals(
					getComponentLore(former),
					getComponentLore(latter))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Retrieves the display name from a given meta.
	 *
	 * @param meta The meta to retrieve the display name from.
	 * @return Returns the display name of an item.
	 */
	public static TextComponent getComponentDisplayName(@Nonnull final ItemMeta meta) {
		return ChatUtils.fromBaseComponents(meta.getDisplayNameComponent());
	}

	/**
	 * Sets a given display name to a given meta.
	 * @param meta The meta to set the display name to.
	 * @param component The display name to set.
	 */
	public static void setComponentDisplayName(@Nonnull final ItemMeta meta, final TextComponent component) {
		if (ChatUtils.isNullOrEmpty(component)) {
			meta.setDisplayName(null);
			return;
		}
		meta.setDisplayNameComponent(ChatUtils.toBaseComponents(component));
	}

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
	 * Retrieves the lore from a given item meta.
	 *
	 * @param meta The item meta to retrieve the lore from.
	 * @return Returns the lore, which is never null.
	 */
	@Nonnull
	public static List<TextComponent> getComponentLore(@Nonnull final ItemMeta meta) {
		final List<BaseComponent[]> lore = meta.getLoreComponents();
		if (CollectionUtils.isEmpty(lore)) {
			return new ArrayList<>();
		}
		return lore.stream()
				.map(ChatUtils::fromBaseComponents)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	/**
	 * Sets the lore to a given item meta.
	 *
	 * @param meta The meta to set the lore to.
	 * @param lines The lore lines to set.
	 */
	public static void setComponentLore(@Nonnull final ItemMeta meta, final TextComponent... lines) {
		if (ArrayUtils.isEmpty(lines)) {
			meta.setLore(null);
			return;
		}
		meta.setLoreComponents(Stream.of(lines)
				.map(ChatUtils::toBaseComponents)
				.collect(Collectors.toList()));
	}

	/**
	 * Sets the lore to a given item meta.
	 *
	 * @param meta The meta to set the lore to.
	 * @param lines The lore lines to set.
	 */
	public static void setComponentLore(@Nonnull final ItemMeta meta, final List<TextComponent> lines) {
		if (CollectionUtils.isEmpty(lines)) {
			meta.setLore(null);
			return;
		}
		meta.setLoreComponents(lines.stream()
				.map(ChatUtils::toBaseComponents)
				.collect(Collectors.toList()));
	}

	/**
	 * Appends lore to an item meta.
	 *
	 * @param meta The item meta to append the lore to.
	 * @param lines The lore to append to the item meta.
	 */
	public static void addLore(@Nonnull final ItemMeta meta, final String... lines) {
		addLore(meta, false, lines);
	}

	/**
	 * Appends lore to an item meta.
	 *
	 * @param meta The item meta to append the lore to.
	 * @param lines The lore to append to the item meta.
	 */
	public static void addLore(@Nonnull final ItemMeta meta, final List<String> lines) {
		addLore(meta, false, lines);
	}

	/**
	 * Adds lore to an item meta, either by appending or prepending.
	 *
	 * @param meta The item meta to append the lore to.
	 * @param prepend If set to true, the lore will be prepended instead of appended.
	 * @param lines The lore to append to the item meta.
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
	 * Adds lore to an item meta, either by appending or prepending.
	 *
	 * @param meta The item meta to append the lore to.
	 * @param prepend If set to true, the lore will be prepended instead of appended.
	 * @param lines The lore to append to the item meta.
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
	 * Clears the lore from an item meta.
	 *
	 * @param meta The item meta to clear the lore of.
	 */
	public static void clearLore(@Nonnull final ItemMeta meta) {
		meta.setLore(null);
	}

	/**
	 * Appends lore to an item meta.
	 *
	 * @param meta The item meta to append the lore to.
	 * @param lines The lore to append to the item meta.
	 */
	public static void addComponentLore(@Nonnull final ItemMeta meta, final TextComponent... lines) {
		addComponentLore(meta, false, lines);
	}

	/**
	 * Appends lore to an item meta.
	 *
	 * @param meta The item meta to append the lore to.
	 * @param lines The lore to append to the item meta.
	 */
	public static void addComponentLore(@Nonnull final ItemMeta meta, final List<TextComponent> lines) {
		addComponentLore(meta, false, lines);
	}

	/**
	 * Adds lore to an item meta, either by appending or prepending.
	 *
	 * @param meta The item meta to append the lore to.
	 * @param prepend If set to true, the lore will be prepended instead of appended.
	 * @param lines The lore to append to the item meta.
	 */
	public static void addComponentLore(@Nonnull final ItemMeta meta,
										final boolean prepend,
										final TextComponent... lines) {
		if (ArrayUtils.isEmpty(lines)) {
			return;
		}
		final List<TextComponent> lore = getComponentLore(meta);
		if (prepend) {
			ArrayUtils.reverse(lines);
			for (final TextComponent line : lines) {
				lore.add(0, line);
			}
		}
		else {
			CollectionUtils.addAll(lore, lines);
		}
		setComponentLore(meta, lore);
	}

	/**
	 * Adds lore to an item meta, either by appending or prepending.
	 *
	 * @param meta The item meta to append the lore to.
	 * @param prepend If set to true, the lore will be prepended instead of appended.
	 * @param lines The lore to append to the item meta.
	 */
	public static void addComponentLore(@Nonnull final ItemMeta meta,
										final boolean prepend,
										final List<TextComponent> lines) {
		if (CollectionUtils.isEmpty(lines)) {
			return;
		}
		final List<TextComponent> lore = getComponentLore(meta);
		if (prepend) {
			Collections.reverse(lines);
			for (final TextComponent line : lines) {
				lore.add(0, line);
			}
		}
		else {
			lore.addAll(lines);
		}
		setComponentLore(meta, lore);
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
