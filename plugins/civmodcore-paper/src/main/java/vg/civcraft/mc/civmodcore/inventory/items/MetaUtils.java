package vg.civcraft.mc.civmodcore.inventory.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.kyori.adventure.text.Component;
import org.apache.commons.collections4.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;

/**
 * Class of static utilities for when you already have an instance of {@link ItemMeta}, such as inside of
 * {@link ItemUtils#handleItemMeta(ItemStack, Predicate)}'s handler, thus all the methods defined below will assume the
 * presence of a valid meta instance.
 */
public final class MetaUtils {

    /**
     * Determines whether two item metas are functionally identical.
     *
     * @param former The first item meta.
     * @param latter The second item meta.
     * @return Returns true if both item metas are functionally identical.
     */
    public static boolean areMetasEqual(@Nullable final ItemMeta former,
                                        @Nullable final ItemMeta latter) {
        if (former == latter) {
            return true;
        }
        if (former == null || latter == null) {
            return false;
        }
        // Create a version of the items that do not have display names or lore, since those are the pain points
        final ItemMeta fakeFormer = former.clone();
        final ItemMeta fakeLatter = latter.clone();
        fakeFormer.displayName(null);
        fakeFormer.lore(null);
        fakeLatter.displayName(null);
        fakeLatter.lore(null);
        if (!Bukkit.getItemFactory().equals(fakeFormer, fakeLatter)) {
            return false;
        }
        // And compare the display name and lore manually
        if (former.hasDisplayName() != latter.hasDisplayName()) {
            return false;
        }
        if (former.hasDisplayName()) {
            if (!ChatUtils.areComponentsEqual(
                former.displayName(),
                latter.displayName())) {
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
     * Retrieves the lore from a given item meta.
     *
     * @param meta The item meta to retrieve the lore from.
     * @return Returns the lore, which is never null.
     */
    @NotNull
    public static List<Component> getComponentLore(@NotNull final ItemMeta meta) {
        final List<Component> lore = meta.lore();
        if (lore == null) {
            return new ArrayList<>(0);
        }
        return lore;
    }

    /**
     * Sets the lore to a given item meta.
     *
     * @param meta  The meta to set the lore to.
     * @param lines The lore lines to set.
     */
    public static void setComponentLore(@NotNull final ItemMeta meta,
                                        @Nullable final Component... lines) {
        setComponentLore(meta, lines == null ? null : Arrays.asList(lines));
    }

    /**
     * Sets the lore to a given item meta.
     *
     * @param meta  The meta to set the lore to.
     * @param lines The lore lines to set.
     */
    public static void setComponentLore(@NotNull final ItemMeta meta,
                                        @Nullable List<Component> lines) {
        if (lines == null) {
            clearLore(meta);
            return;
        }
        lines = new ArrayList<>(lines);
        lines.removeIf(Objects::isNull);
        meta.lore(lines);
    }

    /**
     * Clears the lore from an item meta.
     *
     * @param meta The item meta to clear the lore of.
     */
    public static void clearLore(@NotNull final ItemMeta meta) {
        meta.lore(null);
    }

    /**
     * Appends lore to an item meta.
     *
     * @param meta  The item meta to append the lore to.
     * @param lines The lore to append to the item meta.
     */
    public static void addComponentLore(@NotNull final ItemMeta meta,
                                        @Nullable final Component... lines) {
        addComponentLore(meta, false, lines);
    }

    /**
     * Appends lore to an item meta.
     *
     * @param meta  The item meta to append the lore to.
     * @param lines The lore to append to the item meta.
     */
    public static void addComponentLore(@NotNull final ItemMeta meta,
                                        @Nullable final List<Component> lines) {
        addComponentLore(meta, false, lines);
    }

    /**
     * Adds lore to an item meta, either by appending or prepending.
     *
     * @param meta    The item meta to append the lore to.
     * @param prepend If set to true, the lore will be prepended instead of appended.
     * @param lines   The lore to append to the item meta.
     */
    public static void addComponentLore(@NotNull final ItemMeta meta,
                                        final boolean prepend,
                                        @Nullable final Component... lines) {
        addComponentLore(meta, prepend, lines == null ? null : Arrays.asList(lines));
    }

    /**
     * Adds lore to an item meta, either by appending or prepending.
     *
     * @param meta    The item meta to append the lore to.
     * @param prepend If set to true, the lore will be prepended instead of appended.
     * @param lines   The lore to append to the item meta.
     */
    public static void addComponentLore(@NotNull final ItemMeta meta,
                                        final boolean prepend,
                                        @Nullable List<Component> lines) {
        if (CollectionUtils.isEmpty(lines)) {
            return;
        }
        lines = new ArrayList<>(lines);
        lines.removeIf(Objects::isNull);
        final List<Component> lore = getComponentLore(meta);
        if (prepend) {
            Collections.reverse(lines);
            for (final Component line : lines) {
                lore.add(0, line);
            }
        } else {
            lore.addAll(lines);
        }
        meta.lore(lore);
    }

    // ------------------------------------------------------------
    // Deprecated Functions
    // ------------------------------------------------------------

    /**
     * Retrieves the lore from a given item meta.
     *
     * @param meta The item meta to retrieve the lore from.
     * @return Returns the lore, which is never null.
     * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure.
     * Use {@link #getComponentLore(ItemMeta)} instead.
     */
    @Deprecated
    @NotNull
    public static List<String> getLore(@NotNull final ItemMeta meta) {
        final List<String> lore = meta.getLore();
        if (lore == null) {
            return new ArrayList<>(0);
        }
        return lore;
    }

    /**
     * Appends lore to an item meta.
     *
     * @param meta  The item meta to append the lore to.
     * @param lines The lore to append to the item meta.
     * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure.
     * Use {@link #addComponentLore(ItemMeta, Component...)} instead.
     */
    @Deprecated
    public static void addLore(@NotNull final ItemMeta meta,
                               @Nullable final String... lines) {
        addLore(meta, false, lines);
    }

    /**
     * Appends lore to an item meta.
     *
     * @param meta  The item meta to append the lore to.
     * @param lines The lore to append to the item meta.
     * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure.
     * Use {@link #addComponentLore(ItemMeta, List)} instead.
     */
    @Deprecated
    public static void addLore(@NotNull final ItemMeta meta,
                               @Nullable final List<String> lines) {
        addLore(meta, false, lines);
    }

    /**
     * Adds lore to an item meta, either by appending or prepending.
     *
     * @param meta    The item meta to append the lore to.
     * @param prepend If set to true, the lore will be prepended instead of appended.
     * @param lines   The lore to append to the item meta.
     * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure.
     * Use {@link #addComponentLore(ItemMeta, boolean, Component...)} instead.
     */
    @Deprecated
    public static void addLore(@NotNull final ItemMeta meta,
                               final boolean prepend,
                               @Nullable final String... lines) {
        addLore(meta, prepend, lines == null ? null : Arrays.asList(lines));
    }

    /**
     * Adds lore to an item meta, either by appending or prepending.
     *
     * @param meta    The item meta to append the lore to.
     * @param prepend If set to true, the lore will be prepended instead of appended.
     * @param lines   The lore to append to the item meta.
     * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure.
     * Use {@link #addComponentLore(ItemMeta, boolean, List)} instead.
     */
    @Deprecated
    public static void addLore(@NotNull final ItemMeta meta,
                               final boolean prepend,
                               @Nullable List<String> lines) {
        if (CollectionUtils.isEmpty(lines)) {
            return;
        }
        lines = new ArrayList<>(lines);
        lines.removeIf(Objects::isNull);
        final List<String> lore = getLore(meta);
        if (prepend) {
            Collections.reverse(lines);
            for (final String line : lines) {
                lore.add(0, line);
            }
        } else {
            lore.addAll(lines);
        }
        meta.setLore(lore);
    }

}
