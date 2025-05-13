package vg.civcraft.mc.civmodcore.inventory.items.custom.migrations;

import net.minecraft.core.component.DataComponentHolder;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public sealed interface ItemMigration<T> {
    void doMigration(
        @NotNull T item
    );

    // TODO: Switch this to PaperMC DataComponentHolder on 1.21.5
    non-sealed interface OfData extends ItemMigration<DataComponentHolder> {

    }

    non-sealed interface OfMeta extends ItemMigration<ItemMeta> {

    }

    /**
     * This is a comparatively dangerous migration type. In the context of migrations, this is akin to using sudo, so
     * only use it when you really NEED to: if you're just changing some meta or some data components, use a
     * {@link OfData} or a {@link OfMeta} instead.
     *
     * @apiNote This is tagged as experimental and not given a convenience shortcut to drive the above point home.
     */
    @ApiStatus.Experimental
    non-sealed interface OfItem extends ItemMigration<ItemStack> {

    }

    /**
     * This is a comparatively dangerous migration type. In the context of migrations, this is akin to using sudo, so
     * only use it when you really NEED to: if you're just changing some meta or some data components, use a
     * {@link OfData} or a {@link OfMeta} instead.
     *
     * @apiNote This is tagged as experimental and not given a convenience shortcut to drive the above point home.
     */
    @ApiStatus.Experimental
    non-sealed interface OfNms extends ItemMigration<net.minecraft.world.item.ItemStack> {

    }

    static void migrate(
        final @NotNull ItemStack item,
        final @NotNull ItemMigration<?> migration
    ) {
        switch (migration) {
            case final ItemMigration.OfData dataMigration -> dataMigration.doMigration(CraftItemStack.unwrap(item));
            case final ItemMigration.OfMeta metaMigration -> item.editMeta(metaMigration::doMigration);
            case final ItemMigration.OfItem itemMigration -> itemMigration.doMigration(item);
            case final ItemMigration.OfNms nmsMigration -> nmsMigration.doMigration(CraftItemStack.unwrap(item));
        }
    }
}
