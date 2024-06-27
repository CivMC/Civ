package vg.civcraft.mc.civmodcore.pdc;

import java.util.Objects;
import java.util.function.Function;
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

    /**
     * Convenience method to create a {@link PersistentDataType} for a flatly-valued type, like a String or an integer
     * (as opposed to a record or other type of object that warrants needing the {@link PersistentDataAdapterContext}).
     */
    public static <P, C> @NotNull AbstractPersistentDataType<P, C> flatValued(
        final @NotNull Class<P> primitiveClass,
        final @NotNull Class<C> complexClass,
        final @NotNull Function<@NotNull C, @NotNull P> encoder,
        final @NotNull Function<@NotNull P, @NotNull C> decoder
    ) {
        Objects.requireNonNull(encoder);
        Objects.requireNonNull(decoder);
        return new AbstractPersistentDataType<>(primitiveClass, complexClass) {
            @Override
            public @NotNull P toPrimitive(
                final @NotNull C value,
                final @NotNull PersistentDataAdapterContext adapter
            ) {
                return encoder.apply(value);
            }
            @Override
            public @NotNull C fromPrimitive(
                final @NotNull P raw,
                final @NotNull PersistentDataAdapterContext adapter
            ) {
                return decoder.apply(raw);
            }
        };
    }
}
