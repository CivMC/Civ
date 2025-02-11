package vg.civcraft.mc.civmodcore.datacomponent;

import io.papermc.paper.datacomponent.DataComponentBuilder;
import io.papermc.paper.datacomponent.DataComponentType;
import java.util.Set;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * This is a pseudo-interface for the data-component methods on {@link org.bukkit.inventory.ItemStack}, with the
 * presumption that such a component system will be expanded to other things (like entities) at a later date, so we
 * might as well do the abstraction now (which is just a delegate interface). If PaperMC adds its own interface, we
 * can just delete this and refer to that with little to no refactoring necessary.
 */
@SuppressWarnings("UnstableApiUsage")
public interface DataComponentHolder {
    @Contract(pure = true)
    <T> @Nullable T get(
        final DataComponentType.@NotNull Valued<T> type
    );

    @Contract(pure = true)
    boolean has(
        final @NotNull DataComponentType type
    );

    @Contract("-> new")
    @Unmodifiable Set<@NotNull DataComponentType> getTypes();

    <T> void set(
        final DataComponentType.@NotNull Valued<T> type,
        final @NotNull DataComponentBuilder<T> valueBuilder
    );

    <T> void set(
        final DataComponentType.@NotNull Valued<T> type,
        final @NotNull T value
    );

    void set(
        final DataComponentType.@NotNull NonValued type
    );

    void unset(
        final @NotNull DataComponentType type
    );

    void reset(
        final @NotNull DataComponentType type
    );

    boolean isOverridden(
        final @NotNull DataComponentType type
    );

    static @NotNull DataComponentHolder ofItem(
        final @NotNull ItemStack item
    ) {
        return new ItemDataComponentHolder(item);
    }
}
