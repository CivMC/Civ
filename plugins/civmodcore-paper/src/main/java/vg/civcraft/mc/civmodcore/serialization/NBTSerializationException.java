package vg.civcraft.mc.civmodcore.serialization;

/**
 * Exception to be used within {@link NBTSerializable#serialize(NBTCompound)} and
 * {@link NBTSerializable#deserialize(NBTCompound)}.
 */
public class NBTSerializationException extends RuntimeException {

	private static final long serialVersionUID = 606023177729327630L;

	public NBTSerializationException() {
		super();
	}

	public NBTSerializationException(String message) {
		super(message);
	}

	public NBTSerializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public NBTSerializationException(Throwable cause) {
		super(cause);
	}

}
