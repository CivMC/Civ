package vg.civcraft.mc.civmodcore.serialization;

/**
 * Class designed to encode and decode directly on NBT rather than use cache fields.
 */
public abstract class NBTCache implements NBTSerializable {

	protected final NBTCompound nbt = new NBTCompound();

	@Override
	public void serialize(NBTCompound other) {
		other.adopt(this.nbt);
	}

	@Override
	public void deserialize(NBTCompound other) {
		this.nbt.adopt(other);
	}

}
