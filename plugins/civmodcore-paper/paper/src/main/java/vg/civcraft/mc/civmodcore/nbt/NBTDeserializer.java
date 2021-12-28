package vg.civcraft.mc.civmodcore.nbt;

import javax.annotation.Nonnull;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;

/**
 * Function that's returned by {@link NBTSerialization#getDeserializer(Class)} to retrieve a serializable's version of
 * {@link NBTSerializable#fromNBT(NBTCompound)}.
 */
@FunctionalInterface
public interface NBTDeserializer<T extends NBTSerializable> {

	@Nonnull
	T fromNBT(@Nonnull final NBTCompound nbt);

}
