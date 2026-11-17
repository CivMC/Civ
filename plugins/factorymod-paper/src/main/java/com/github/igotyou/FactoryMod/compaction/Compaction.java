package com.github.igotyou.FactoryMod.compaction;

import java.util.List;
import java.util.NoSuchElementException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.items.MetaUtils;

public final class Compaction {
    public static final String COMPACTED_ITEM_LORE = "Compacted Item";

    /// Marks the given item as compacted.
    public static void markAsCompacted(
        final @NotNull ItemMeta meta
    ) {
        MetaUtils.addLore(meta, List.of(COMPACTED_ITEM_LORE));
    }

    /// Removes all lines of lore that match [#COMPACTED_ITEM_LORE]
    public static void demarkAsCompacted(
        final @NotNull ItemMeta meta
    ) {
        final List<Component> lore = MetaUtils.getComponentLore(meta);
        lore.removeIf(Compaction::isCompactedLoreLine);
        meta.lore(lore);
    }

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
        return hasCompactedLore(meta);
    }

    /// Checks whether an item's last lore-line matches the expected compacted lore.
    public static boolean hasCompactedLore(
        final @NotNull ItemMeta meta
    ) {
        final List<Component> lore = meta.lore();
        if (lore == null) {
            return false;
        }
        final Component last;
        try {
            last = lore.getLast();
        }
        catch (final NoSuchElementException _) {
            return false;
        }
        return isCompactedLoreLine(last);
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
}
