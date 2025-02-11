package vg.civcraft.mc.civmodcore.inventory.items.updater.migrations;

import net.minecraft.core.component.DataComponentHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public sealed interface ItemMigration<T> {
    void doMigration(
        @NotNull T item
    );

    non-sealed interface DataMigration extends ItemMigration<DataComponentHolder> {

    }

    non-sealed interface MetaMigration extends ItemMigration<ItemMeta> {

    }

    /**
     * This is a comparatively dangerous migration type. In the context of migrations, this is akin to using sudo, so
     * only use it when you really NEED to: if you're just changing some meta or some data components, use a
     * {@link MetaMigration} or a {@link DataMigration} instead.
     *
     * @apiNote This is tagged as experimental and not given a convenience shortcut to drive the above point home.
     */
    @ApiStatus.Experimental
    non-sealed interface ItemStackMigration extends ItemMigration<ItemStack> {

    }
}
