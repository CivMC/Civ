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
	public enum AddLore { NO, YES, YES_IF_ABSENT }

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
	 * Checks whether a given item is marked as compacted via its PDC or its lore.
	 */
	public static boolean isCompacted(
		final @NotNull ItemMeta meta
	) {
		return isCompacted(meta, true);
	}

	/**
	 * Checks whether a given item is marked as compacted via its PDC.
	 *
	 * @param checkLore Whether to also check its lore.
	 */
	public static boolean isCompacted(
		final @NotNull ItemMeta meta,
		final boolean checkLore
	) {
		if (meta.getPersistentDataContainer().getOrDefault(COMPACTED_KEY, PersistentDataTypes.BOOLEAN, false)) {
			return true;
		}
		if (checkLore) {
			return hasCompactedLore(meta);
		}
		return false;
	}

	private static boolean hasCompactedLore(
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
	 * Marks the given item as compacted.
	 */
	public static void markAsCompacted(
		final @NotNull ItemMeta meta,
		final @NotNull AddLore addLore
	) {
		meta.getPersistentDataContainer().set(COMPACTED_KEY, PersistentDataTypes.BOOLEAN, true);
		switch (addLore) {
			case YES -> addCompactedLore(meta, false);
			case YES_IF_ABSENT -> addCompactedLore(meta, true);
		}
	}

	/**
	 * Adds the "Compacted Item" lore.
	 *
	 * @param skipIfAlreadyExists Whether to skip adding the lore if it already exists within the lore.
	 */
	public static void addCompactedLore(
		final @NotNull ItemMeta meta,
		final boolean skipIfAlreadyExists
	) {
		if (skipIfAlreadyExists && hasCompactedLore(meta)) {
			return;
		}
		MetaUtils.addComponentLore(meta, List.of(Component.text(COMPACTED_ITEM_LORE)));
	}

	/**
	 * Removes the marking that marks the item as compacted, as well as the lore text, if they exist.
	 */
	public static void removeCompactedMarking(
		final @NotNull ItemMeta meta
	) {
		meta.getPersistentDataContainer().remove(COMPACTED_KEY);

		final List<Component> lore = MetaUtils.getComponentLore(meta);
		lore.removeIf(Compaction::isCompactedLoreLine);
		meta.lore(lore);
	}
}
