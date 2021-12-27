package vg.civcraft.mc.civmodcore.nbt;

import java.io.Serial;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;

/**
 * Exception that ought to be used within {@link NBTSerializable#toNBT(NBTCompound)} and
 * {@link NBTSerializable#fromNBT(NBTCompound)}.
 */
public class NBTSerializationException extends RuntimeException {

	@Serial
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