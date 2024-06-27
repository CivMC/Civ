package vg.civcraft.mc.civmodcore.pdc;

import java.util.Objects;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractPersistentDataType<P, C> implements PersistentDataType<P, C> {
    protected final Class<P> primitiveClass;
    protected final Class<C> complexClass;

    public AbstractPersistentDataType(
        final @NotNull Class<P> primitiveClass,
        final @NotNull Class<C> complexClass
    ) {
        this.primitiveClass = Objects.requireNonNull(primitiveClass);
        this.complexClass = Objects.requireNonNull(complexClass);
    }

    @Override
    public final @NotNull Class<P> getPrimitiveType() {
        return this.primitiveClass;
    }

    @Override
    public final @NotNull Class<C> getComplexType() {
        return this.complexClass;
    }

    @Override
    public abstract @NotNull P toPrimitive(
        @NotNull C value,
        @NotNull PersistentDataAdapterContext adapter
    );

    @Override
    public abstract @NotNull C fromPrimitive(
        @NotNull P raw,
        @NotNull PersistentDataAdapterContext adapter
    );

}
