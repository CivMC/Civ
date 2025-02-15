package vg.civcraft.mc.civmodcore.inventory.items.updater.impl;

import java.util.Objects;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.events.EventUtils;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;
import vg.civcraft.mc.civmodcore.inventory.items.updater.ItemUpdater;
import vg.civcraft.mc.civmodcore.inventory.items.updater.migrations.ItemMigrations;

/**
 * Use this in lieu of {@link vg.civcraft.mc.civmodcore.inventory.items.updater.impl.CustomItemsUpdater} when you only
 * have a single custom item that needs updating, as to avoid the overhead of searching a HashMap for your singular
 * custom item type.
 */
public class CustomItemUpdater implements ItemUpdater {
    public final String customKey;
    public final ItemMigrations migrations;

    public CustomItemUpdater(
        final @NotNull String customKey
    ) {
        this(ItemMigrations.DEFAULT_VERSION_KEY, customKey);
    }

    public CustomItemUpdater(
        final @NotNull NamespacedKey versionKey,
        final @NotNull String customKey
    ) {
        this.customKey = Objects.requireNonNull(customKey);
        this.migrations = new ItemMigrations(versionKey);
    }

    @Override
    public boolean updateItem(
        final @NotNull ItemStack item
    ) {
        return CustomItem.isCustomItem(item, this.customKey) && this.migrations.attemptMigration(item);
    }

    public static @NotNull CustomItemUpdater init(
        final @NotNull JavaPlugin plugin,
        final @NotNull String customKey
    ) {
        final var updater = new CustomItemUpdater(customKey);
        EventUtils.registerListener(plugin, DefaultItemUpdaterListeners.wrap(updater));
        return updater;
    }
}
