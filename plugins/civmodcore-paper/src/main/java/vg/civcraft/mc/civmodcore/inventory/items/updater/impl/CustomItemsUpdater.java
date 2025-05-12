package vg.civcraft.mc.civmodcore.inventory.items.updater.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.inventory.items.custom.CustomItem;
import vg.civcraft.mc.civmodcore.inventory.items.updater.ItemUpdater;
import vg.civcraft.mc.civmodcore.inventory.items.updater.migrations.ItemMigrations;

public class CustomItemsUpdater implements ItemUpdater {
    public final NamespacedKey versionKey;
    private final Map<String, ItemMigrations> targets;

    public CustomItemsUpdater() {
        this(ItemMigrations.DEFAULT_VERSION_KEY);
    }

    public CustomItemsUpdater(
        final @NotNull NamespacedKey versionKey
    ) {
        this.versionKey = Objects.requireNonNull(versionKey);
        this.targets = new HashMap<>();
    }

    /**
     * Retrieves a migration list for the given custom key, creating one if it didn't already exist.
     */
    public @NotNull ItemMigrations getMigrationsFor(
        final @NotNull String customKey
    ) {
        return getMigrationsFor(customKey, true);
    }

    /**
     * Retrieves a migration list for the given custom key.
     *
     * @param createIfAbsent Whether to create the list if one doesn't already exist.
     */
    @Contract("_, true -> !null")
    public @Nullable ItemMigrations getMigrationsFor(
        final @NotNull String customKey,
        final boolean createIfAbsent
    ) {
        if (createIfAbsent) {
            return this.targets.computeIfAbsent(customKey, (_k) -> new ItemMigrations(this.versionKey));
        }
        return this.targets.get(customKey);
    }

    public void removeMigrationsFor(
        final @NotNull String customKey
    ) {
        final ItemMigrations migrations = this.targets.remove(customKey);
        if (migrations != null) {
            migrations.clearMigrations();
        }
    }

    public void clearMigrations() {
        final List<ItemMigrations> migrations = List.copyOf(this.targets.values());
        this.targets.clear();
        for (final ItemMigrations migration : migrations) {
            migration.clearMigrations();
        }
    }

    @Override
    public boolean updateItem(
        final @NotNull ItemStack item
    ) {
        final String customKey = CustomItem.getCustomItemKey(item);
        if (customKey == null) {
            return false;
        }
        final ItemMigrations migrations = this.targets.get(customKey);
        if (migrations == null) {
            return false;
        }
        return migrations.attemptMigration(item);
    }

    public static @NotNull CustomItemsUpdater init(
        final @NotNull JavaPlugin plugin
    ) {
        final var updater = new CustomItemsUpdater();
        Bukkit.getPluginManager().registerEvents(DefaultItemUpdaterListeners.wrap(updater), plugin);
        return updater;
    }
}
