package vg.civcraft.mc.civmodcore.inventory.items.compaction;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.items.MetaUtils;
import vg.civcraft.mc.civmodcore.pdc.PersistentDataTypes;

public final class Compaction {
	private static final NamespacedKey COMPACTED_KEY = new NamespacedKey("cmc", "compacted_item");
	public static final String COMPACTED_ITEM_LORE = "Compacted Item";

	/**
	 * Checks whether a given item is marked as compacted via its PDC or its lore.
	 */
	public static boolean isCompacted(
		final ItemStack item
	) {
		if (item == null) {
			return false;
		}
		final ItemMeta meta = item.getItemMeta();
		if (meta == null) {
			return false;
		}
		return isCompacted(meta);
	}

	/**
	 * Checks whether a given item is marked as compacted via its PDC.
	 */
	public static boolean isCompacted(
		final @NotNull ItemMeta meta
	) {
		return meta.getPersistentDataContainer().getOrDefault(COMPACTED_KEY, PersistentDataTypes.BOOLEAN, false);
	}

	/**
	 * Marks the given item as compacted.
	 */
	public static void markAsCompacted(
		final @NotNull ItemMeta meta
	) {
		meta.getPersistentDataContainer().set(COMPACTED_KEY, PersistentDataTypes.BOOLEAN, true);
	}

	/**
	 * Removes the marking that marks the item as compacted, as well as the lore text, if it's present.
	 */
	public static void removeCompactedMarking(
		final @NotNull ItemMeta meta
	) {
		meta.getPersistentDataContainer().remove(COMPACTED_KEY);
	}

	public static boolean hasCompactedLore(
		final @NotNull ItemMeta meta
	) {
		final List<Component> lore = meta.lore();
		if (lore == null) {
			return false;
		}
		for (final Component line : lore) {
			if (isCompactedLoreLine(line)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isCompactedLoreLine(
		final @NotNull Component line
	) {
		final var content = new StringBuilder();
		for (final Component part : line.iterable(ComponentIteratorType.DEPTH_FIRST)) {
			if (part.hasStyling()) {
				return false;
			}
			if (part instanceof final TextComponent textPart) {
				content.append(textPart.content());
			}
		}
		return COMPACTED_ITEM_LORE.contentEquals(content);
	}

	/**
	 * Adds the "Compacted Item" lore.
	 */
	public static void addCompactedLore(
		final @NotNull ItemMeta meta
	) {
		MetaUtils.addComponentLore(meta, List.of(Component.text(COMPACTED_ITEM_LORE)));
	}

	/**
	 * Adds the "Compacted Item" lore in the Bukkit fashion.
	 */
	@SuppressWarnings("deprecation")
	public static void addLegacyCompactedLore(
		final @NotNull ItemMeta meta
	) {
		MetaUtils.addLore(meta, List.of(COMPACTED_ITEM_LORE));
	}

	/**
	 * Removes the "Compacted Item" lore.
	 */
	public static void removeCompactedLore(
		final @NotNull ItemMeta meta
	) {
		final List<Component> lore = MetaUtils.getComponentLore(meta);
		lore.removeIf(Compaction::isCompactedLoreLine);
		MetaUtils.setComponentLore(meta, lore);
	}

	public enum UpgradeResult { SUCCESS, EMPTY_ITEM, ALREADY_COMPACTED, NOT_COMPACTED }

	/**
	 * Attempts to upgrade a legacy compacted item.
	 */
	public static @NotNull UpgradeResult attemptUpgrade(
		final ItemStack item
	) {
		if (item == null) {
			return UpgradeResult.EMPTY_ITEM;
		}
		final ItemMeta meta = item.getItemMeta();
		if (meta == null) {
			return UpgradeResult.EMPTY_ITEM;
		}
		final UpgradeResult result = attemptUpgrade(meta);
		if (result == UpgradeResult.SUCCESS) {
			item.setItemMeta(meta);
		}
		return result;
	}

	/**
	 * Attempts to upgrade a legacy compacted item lore.
	 */
	public static @NotNull UpgradeResult attemptUpgrade(
		final @NotNull ItemMeta meta
	) {
		if (isCompacted(meta)) {
			return UpgradeResult.ALREADY_COMPACTED;
		}
		if (!hasCompactedLore(meta)) {
			return UpgradeResult.NOT_COMPACTED;
		}
		markAsCompacted(meta);
		removeCompactedLore(meta);
		return UpgradeResult.SUCCESS;
	}
}
