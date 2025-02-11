package vg.civcraft.mc.civmodcore.inventory.items.updater.migrations;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;

public class ItemMigrations {
    public static final NamespacedKey DEFAULT_VERSION_KEY = new NamespacedKey(JavaPlugin.getPlugin(CivModCorePlugin.class), "item_version");

    public final NamespacedKey versionKey;
    protected final TreeMap<Integer, ItemMigration<?>> migrations = new TreeMap<>(Integer::compareTo);

    public ItemMigrations(
        final @NotNull NamespacedKey versionKey
    ) {
        this.versionKey = Objects.requireNonNull(versionKey);
    }

    public void registerMigration(
        final @Range(from = 1, to = Integer.MAX_VALUE) int migrationId,
        final @NotNull ItemMigration<?> updater
    ) {
        this.migrations.putIfAbsent(migrationId, updater);
    }

    /** Convenience shortcut */
    public void registerDataMigration(
        final @Range(from = 1, to = Integer.MAX_VALUE) int migrationId,
        final @NotNull ItemMigration.DataMigration updater
    ) {
        registerMigration(migrationId, updater);
    }

    /** Convenience shortcut */
    public void registerMetaMigration(
        final @Range(from = 1, to = Integer.MAX_VALUE) int migrationId,
        final @NotNull ItemMigration.MetaMigration updater
    ) {
        registerMigration(migrationId, updater);
    }

    public int getMigrationVersion(
        final @NotNull ItemStack item
    ) {
        // This is a readonly PDC
        return item.getPersistentDataContainer().getOrDefault(this.versionKey, PersistentDataType.INTEGER, 0);
    }

    public int getMigrationVersion(
        final @NotNull ItemMeta meta
    ) {
        // This is a mutable PDC
        return meta.getPersistentDataContainer().getOrDefault(this.versionKey, PersistentDataType.INTEGER, 0);
    }

    public void setMigrationVersion(
        final @NotNull ItemMeta meta,
        final int version
    ) {
        meta.getPersistentDataContainer().set(this.versionKey, PersistentDataType.INTEGER, version);
    }

    /**
     * Attempts to update the given item according to a series of pre-registered migrations.
     *
     * @param item The item to update, which MUST NOT be an empty item, as determined by a {@link vg.civcraft.mc.civmodcore.inventory.items.ItemUtils#isEmptyItem(ItemStack)} check.
     * @return Whether the item was updated.
     */
    public boolean attemptMigration(
        final @NotNull ItemStack item
    ) {
        final Map<Integer, ItemMigration<?>> pendingMigrations = this.migrations.tailMap(getMigrationVersion(item), false);
        if (pendingMigrations.isEmpty()) {
            return false;
        }
        boolean updated = false;
        for (final Map.Entry<Integer, ItemMigration<?>> entry : pendingMigrations.entrySet()) {
            final ItemMeta meta;
            switch (entry.getValue()) {
                case final ItemMigration.DataMigration dataMigration -> {
                    dataMigration.doMigration(CraftItemStack.unwrap(item));
                    meta = item.getItemMeta();
                }
                case final ItemMigration.MetaMigration metaMigration -> {
                    meta = item.getItemMeta();
                    metaMigration.doMigration(meta);
                }
                case final ItemMigration.ItemStackMigration itemMigration -> {
                    itemMigration.doMigration(item);
                    meta = item.getItemMeta();
                }
            }
            setMigrationVersion(meta, entry.getKey());
            item.setItemMeta(meta);
            updated = true;
        }
        return updated;
    }
}
