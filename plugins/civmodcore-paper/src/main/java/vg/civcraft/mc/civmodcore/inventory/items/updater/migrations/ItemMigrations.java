package vg.civcraft.mc.civmodcore.inventory.items.updater.migrations;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public abstract class ItemMigrations {
    protected final TreeMap<Integer, ItemMigration<?>> migrations = new TreeMap<>(Integer::compareTo);

    public void registerMigration(
        final @Range(from = 1, to = Integer.MAX_VALUE) int migrationId,
        final @NotNull ItemMigration<?> updater
    ) {
        this.migrations.putIfAbsent(migrationId, Objects.requireNonNull(updater));
    }

    /** Convenience shortcut */
    public void registerDataMigration(
        final @Range(from = 1, to = Integer.MAX_VALUE) int migrationId,
        final @NotNull ItemMigration.OfData updater
    ) {
        registerMigration(migrationId, updater);
    }

    /** Convenience shortcut */
    public void registerMetaMigration(
        final @Range(from = 1, to = Integer.MAX_VALUE) int migrationId,
        final @NotNull ItemMigration.OfMeta updater
    ) {
        registerMigration(migrationId, updater);
    }

    public void clearMigrations() {
        this.migrations.clear();
    }

    /**
     * Retrieves the current migration version of a given item.
     *
     * @return Returns the current migration version, or null if it isn't set.
     */
    public abstract @Nullable Integer getMigrationVersion(
        @NotNull ItemStack item
    );

    /**
     * Permanently stores the given migration version onto the given item in a way that
     * {@link #getMigrationVersion(org.bukkit.inventory.ItemStack)} will detect.
     */
    public abstract void setMigrationVersion(
        @NotNull ItemStack item,
        int version
    );

    /**
     * Attempts to update the given item according to a series of pre-registered migrations.
     *
     * @param item The item to update, which MUST NOT be an empty item, as determined by a {@link vg.civcraft.mc.civmodcore.inventory.items.ItemUtils#isEmptyItem(ItemStack)} check.
     * @return Whether the item was updated.
     */
    public boolean attemptMigration(
        final @NotNull ItemStack item
    ) {
        final Integer currentVersion = getMigrationVersion(item);
        final Map<Integer, ItemMigration<?>> pendingMigrations = this.migrations.tailMap(
            Objects.requireNonNullElse(currentVersion, 0),
            currentVersion == null // Include the 0th migration if the version was missing
        );
        boolean updated = false;
        for (final Map.Entry<Integer, ItemMigration<?>> entry : pendingMigrations.entrySet()) {
            ItemMigration.migrate(item, entry.getValue());
            setMigrationVersion(item, entry.getKey());
            updated = true;
        }
        return updated;
    }
}
