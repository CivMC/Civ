package com.untamedears.itemexchange.utility.nbt;

import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.nbt.NbtCompound;

/**
 * Function that's returned by {@link NBTSerialization#getDeserializer(Class)} to retrieve a serializable's version of
 * {@link NBTSerializable#fromNBT(NbtCompound)}.
 */
@Deprecated
@FunctionalInterface
public interface NBTDeserializer<T extends NBTSerializable> {
    @NotNull T fromNBT(
        final @NotNull NbtCompound nbt
    );
}
