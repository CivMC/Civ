package vg.civcraft.mc.civmodcore.pdc;

import java.util.Objects;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.jetbrains.annotations.NotNull;

/**
 * This class enables the easy encoding and decoding of enums.
 */
public class PersistentEnumDataType<T extends Enum<T>> extends AbstractPersistentDataType<String, T> {

    protected final T defaultValue;
    protected final boolean useDefault;

    /**
     * This constructor is used when you want invalid enum decodings to error.
     *
     * @param enumClass The class of the enum.
     */
    public PersistentEnumDataType(@NotNull final Class<T> enumClass) {
        super(String.class, enumClass);
        this.defaultValue = null;
        this.useDefault = false;
    }

    /**
     * This constructor is used when you want invalid enum decodings to fall back to a default value.
     *
     * @param defaultValue The default value that {@link #fromPrimitive(String, PersistentDataAdapterContext)} will
     *                     return if the raw value is null or invalid.
     */
    @SuppressWarnings("unchecked")
    public PersistentEnumDataType(@NotNull final T defaultValue) {
        super(String.class, (Class<T>) defaultValue.getClass());
        this.defaultValue = defaultValue;
        this.useDefault = true;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public String toPrimitive(@NotNull final T instance,
                              @NotNull final PersistentDataAdapterContext adapter) {
        return instance.name();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public T fromPrimitive(@NotNull final String raw,
                           @NotNull final PersistentDataAdapterContext adapter) {
        return this.useDefault ?
            EnumUtils.getEnum(this.complexClass, raw, this.defaultValue) :
            Objects.requireNonNull(EnumUtils.getEnum(this.complexClass, raw),
                String.format(PersistentDataTypes.DECODER_ERROR, this.complexClass.getSimpleName(), raw));
    }

}
