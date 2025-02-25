package vg.civcraft.mc.civmodcore.nbt;

import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;

/**
 * Function that's returned by {@link NBTSerialization#getDeserializer(Class)} to retrieve a serializable's version of
 * {@link NBTSerializable#fromNBT(NBTCompound)}.
 */
@FunctionalInterface
public interface NBTDeserializer<T extends NBTSerializable> {

    @NotNull
    T fromNBT(@NotNull final NBTCompound nbt);

}
