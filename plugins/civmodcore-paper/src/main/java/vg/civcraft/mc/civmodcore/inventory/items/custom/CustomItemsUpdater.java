package vg.civcraft.mc.civmodcore.inventory.items.custom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.inventory.items.updater.ItemUpdater;
import vg.civcraft.mc.civmodcore.inventory.items.updater.listeners.DefaultItemUpdaterListeners;
import vg.civcraft.mc.civmodcore.inventory.items.updater.migrations.ItemMigration;
import vg.civcraft.mc.civmodcore.inventory.items.updater.migrations.ItemMigrations;

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
            return this.targets.computeIfAbsent(customKey, CustomItemMigrations::new);
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
     * <p>This is how this class determines whether a given item is a custom item. You can just override this with a simple
     * call to {@link vg.civcraft.mc.civmodcore.inventory.items.custom.CustomItem#getCustomItemKey(org.bukkit.inventory.ItemStack)},
     * but you may want to add additional logic to support legacy custom items.</p>
     *
     * <pre>{@code
     * // Example
     * @Override
     * public @Nullable String getCustomKeyFrom(@NotNull ItemStack item) {
     *     if (CustomItem.getCustomItem(item) instanceof final String customKey) {
     *         return customKey;
     *     }
     *     if (item.getType() == Material.ENDER_EYE
     *         && ExampleUtils.hasPlainDisplayName(item, "Player Essence")
     *         && ExampleUtils.hasPlainLoreLine(item, "Activity reward used to fuel pearls")
     *     ) {
     *         return "player_essence";
     *     }
     *     if (item.getType() == Material.BONE_BLOCK
     *         && ExampleUtils.hasPlainDisplayName(item, "City Bastion")
     *         && ExampleUtils.hasPlainLoreLine(item, "City bastions block reinforcements and elytra")
     *     ) {
     *         return "city_bastion";
     *     }
     *     // etc
     *     return null;
     * }
     * }</pre>
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

final class CustomItemMigrations extends ItemMigrations {
    private static final NamespacedKey VERSION_KEY = new NamespacedKey(JavaPlugin.getPlugin(CivModCorePlugin.class), "item_version");

    public CustomItemMigrations(
        final @NotNull String customKey
    ) {
        // This is a deliberate 0th migration that ensures that any item
        // being migrated has a custom-item key and item version.
        this.migrations.put(0, (ItemMigration.OfItem) (item) -> {
            CustomItem.setCustomItemKey(item, customKey);
            setMigrationVersion(item, 0);
        });
    }

    @Override
    public @Nullable Integer getMigrationVersion(
        final @NotNull ItemStack item
    ) {
        // This is a readonly PDC
        return item.getPersistentDataContainer().get(VERSION_KEY, PersistentDataType.INTEGER);
    }

    @Override
    public void setMigrationVersion(
        final @NotNull ItemStack item,
        final int version
    ) {
        item.editPersistentDataContainer((pdc) -> pdc.set(VERSION_KEY, PersistentDataType.INTEGER, version));
    }
}
