package vg.civcraft.mc.civmodcore.serialization;

public class ExampleSerializable implements NBTSerializable {

	private String message;

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public void serialize(NBTCompound nbt) throws NBTSerializationException {
		nbt.setString("message", this.message);
	}

	@Override
	public void deserialize(NBTCompound nbt) throws NBTSerializationException {
		this.message = nbt.getString("message");
	}

}
