package vg.civcraft.mc.civmodcore.inventory.items.updater.migrations;

import net.minecraft.core.component.DataComponentHolder;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public sealed interface ItemMigration<T> {
    void doMigration(
        @NotNull T item
    );

    non-sealed interface DataMigration extends ItemMigration<DataComponentHolder> {

    }

    non-sealed interface MetaMigration extends ItemMigration<ItemMeta> {

    }
}
