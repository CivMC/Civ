package vg.civcraft.mc.civmodcore.inventory.items.custom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vg.civcraft.mc.civmodcore.inventory.items.custom.migrations.ItemMigrations;
import vg.civcraft.mc.civmodcore.inventory.items.updater.ItemUpdater;
import vg.civcraft.mc.civmodcore.inventory.items.updater.listeners.DefaultItemUpdaterListeners;

public abstract class CustomItemsUpdater implements ItemUpdater {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final Map<String, ItemMigrations> targets = new HashMap<>();

    /**
     * Retrieves a migration list for the given custom key, creating one if it didn't already exist.
     */
    public @NotNull ItemMigrations getMigrationsFor(
        final @NotNull String customKey
    ) {
        return getMigrationsFor(Objects.requireNonNull(customKey), true);
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
        Objects.requireNonNull(customKey);
        if (createIfAbsent) {
            return this.targets.computeIfAbsent(customKey, (_k) -> new ItemMigrations(customKey));
        }
        return this.targets.get(customKey);
    }

    public void removeMigrationsFor(
        final @NotNull String customKey
    ) {
        final ItemMigrations migrations = this.targets.remove(Objects.requireNonNull(customKey));
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

    /**
     * This is how this class determines whether an item is a custom item. You can just override with <i>just</i> a call
     * to {@link vg.civcraft.mc.civmodcore.inventory.items.custom.CustomItem#getCustomItemKey(org.bukkit.inventory.ItemStack)},
     * but you can also include some logic that allows support for pre-{@link vg.civcraft.mc.civmodcore.inventory.items.custom.CustomItem}
     * custom items.
     */
    protected abstract @Nullable String getCustomKeyFrom(
        @NotNull ItemStack item
    );

    @Override
    public boolean updateItem(
        final @NotNull ItemStack item
    ) {
        final String customKey = getCustomKeyFrom(item);
        if (customKey == null) {
            return false;
        }
        final ItemMigrations migrations = this.targets.get(customKey);
        if (migrations == null) {
            return false;
        }
        return migrations.attemptMigration(item);
    }

    // ============================================================
    // Defaults
    // ============================================================

    @Contract("_, _ -> param2")
    public static <T extends CustomItemsUpdater> @NotNull T init(
        final @NotNull JavaPlugin plugin,
        final @NotNull T updater
    ) {
        Bukkit.getPluginManager().registerEvents(DefaultItemUpdaterListeners.wrap(updater), plugin);
        return updater;
    }
}
