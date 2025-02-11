package vg.civcraft.mc.civmodcore.datacomponent;

import io.papermc.paper.datacomponent.DataComponentBuilder;
import io.papermc.paper.datacomponent.DataComponentType;
import java.util.Objects;
import java.util.Set;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

@SuppressWarnings("UnstableApiUsage")
final class ItemDataComponentHolder implements DataComponentHolder {
    private final ItemStack item;

    ItemDataComponentHolder(
        final @NotNull ItemStack item
    ) {
        this.item = Objects.requireNonNull(item);
    }

    @Override
    @Contract(pure = true)
    public <T> @Nullable T get(
        final DataComponentType.@NotNull Valued<T> type
    ) {
        return this.item.getData(type);
    }

    @Override
    @Contract(pure = true)
    public boolean has(
        final @NotNull DataComponentType type
    ) {
        return this.item.hasData(type);
    }

    @Override
    @Contract("-> new")
    public @Unmodifiable Set<@NotNull DataComponentType> getTypes() {
        return this.item.getDataTypes();
    }

    @Override
    public <T> void set(
        final DataComponentType.@NotNull Valued<T> type,
        final @NotNull DataComponentBuilder<T> valueBuilder
    ) {
        this.item.setData(type, valueBuilder);
    }

    @Override
    public <T> void set(
        final DataComponentType.@NotNull Valued<T> type,
        final @NotNull T value
    ) {
        this.item.setData(type, value);
    }

    @Override
    public void set(
        final DataComponentType.@NotNull NonValued type
    ) {
        this.item.setData(type);
    }

    @Override
    public void unset(
        final @NotNull DataComponentType type
    ) {
        this.item.unsetData(type);
    }

    @Override
    public void reset(
        final @NotNull DataComponentType type
    ) {
        this.item.resetData(type);
    }

    @Override
    public boolean isOverridden(
        final @NotNull DataComponentType type
    ) {
        return this.item.isDataOverridden(type);
    }
}
