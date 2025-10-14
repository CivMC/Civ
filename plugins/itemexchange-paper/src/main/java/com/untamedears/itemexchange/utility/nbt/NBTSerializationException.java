package com.untamedears.itemexchange.utility.nbt;

import java.io.Serial;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.nbt.NbtCompound;
import vg.civcraft.mc.civmodcore.nbt.exceptions.NbtException;

/**
 * Exception that ought to be used within {@link NBTSerializable#toNBT(NbtCompound)} and {@link NBTSerializable#fromNBT(NbtCompound)}.
 */
@Deprecated
public class NBTSerializationException extends NbtException {
    @Serial
    private static final long serialVersionUID = 606023177729327630L;

    public NBTSerializationException() {
        super();
    }

    public NBTSerializationException(
        final @NotNull String message
    ) {
        super(message);
    }

    public NBTSerializationException(
        final @NotNull String message,
        final @NotNull Throwable cause
    ) {
        super(message, cause);
    }

    public NBTSerializationException(
        final @NotNull Throwable cause
    ) {
        super(cause);
    }
}
