package vg.civcraft.mc.civmodcore.serialization;

import vg.civcraft.mc.civmodcore.api.NBTCompound;

public class ExampleSerializable implements NBTSerializable {

	private String message;

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Serializes a class into an NBTCompound.
	 *
	 * @param nbt The NBTCompound to serialize into, which <i>should</i> NEVER be null, so feel free to throw an
	 * {@link NBTSerializationException} if it is. You can generally assume that the nbt compound is new and therefore
	 * empty, but you may wish to check or manually {@link NBTCompound#clear() empty it}, though the latter may cause
	 * other issues.
	 * @throws NBTSerializationException This is thrown if the implementation has a fatal error serializing.
	 */
	@Override
	public void serialize(NBTCompound nbt) throws NBTSerializationException {
		nbt.setString("message", this.message);
	}

	/**
	 * Serializes a class into an NBTCompound.
	 *
	 * @param nbt The NBTCompound to deserialize from, which <i>should</i> NEVER be null, so feel free to throw an
	 * {@link NBTSerializationException} if it is.
	 * @throws NBTSerializationException This is thrown if the implementation has a fatal error deserializing.
	 */
	@Override
	public void deserialize(NBTCompound nbt) throws NBTSerializationException {
		this.message = nbt.getString("message");
	}

}
