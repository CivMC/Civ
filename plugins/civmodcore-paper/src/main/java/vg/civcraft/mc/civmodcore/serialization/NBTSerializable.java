package vg.civcraft.mc.civmodcore.serialization;

import vg.civcraft.mc.civmodcore.api.NBTCompound;

/**
 * Interface that grants a class access to {@link NBTSerialization NBTSerialization's} process of converting a class to
 * and from an NBTCompound.
 * */
public interface NBTSerializable {

	/**
	 * Serializes a class into an NBTCompound.
	 *
	 * @param nbt The NBTCompound to serialize into, which <i>should</i> NEVER be null, so feel free to throw an
	 * {@link NBTSerializationException} if it is. You can generally assume that the nbt compound is new and therefore
	 * empty, but you may wish to check or manually {@link NBTCompound#clear() empty it}, though the latter may cause
	 * other issues.
	 *
	 * @throws NBTSerializationException This is thrown if the implementation has a fatal error serializing.
	 * */
	void serialize(NBTCompound nbt) throws NBTSerializationException;

	/**
	 * Deserializes a class into an NBTCompound.
	 *
	 * @param nbt The NBTCompound to deserialize from, which <i>should</i> NEVER be null, so feel free to throw an
	 * {@link NBTSerializationException} if it is.
	 *
	 * @throws NBTSerializationException This is thrown if the implementation has a fatal error deserializing.
	 * */
	void deserialize(NBTCompound nbt) throws NBTSerializationException;

}
