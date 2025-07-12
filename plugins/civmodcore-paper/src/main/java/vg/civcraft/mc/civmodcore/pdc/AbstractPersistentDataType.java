package vg.civcraft.mc.civmodcore.pdc;

import java.util.Objects;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractPersistentDataType<P, C> implements PersistentDataType<P, C> {

    protected final Class<P> primitiveClass;
    protected final Class<C> complexClass;

    public AbstractPersistentDataType(@NotNull final Class<P> primitiveClass,
                                      @NotNull final Class<C> complexClass) {
        this.primitiveClass = Objects.requireNonNull(primitiveClass);
        this.complexClass = Objects.requireNonNull(complexClass);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public final Class<P> getPrimitiveType() {
        return this.primitiveClass;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public final Class<C> getComplexType() {
        return this.complexClass;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public abstract P toPrimitive(@NotNull C instance,
                                  @NotNull PersistentDataAdapterContext adapter);

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public abstract C fromPrimitive(@NotNull P raw,
                                    @NotNull PersistentDataAdapterContext adapter);

}
