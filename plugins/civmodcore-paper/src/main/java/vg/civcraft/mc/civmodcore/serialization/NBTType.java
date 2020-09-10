package vg.civcraft.mc.civmodcore.serialization;

/**
 * NBT Type IDs for usage with {@link NBTCompound#hasKeyOfType(String, int)}.
 */
public final class NBTType {

	public static final byte END = (byte) 0;
	public static final byte BYTE = (byte) 1;
	public static final byte SHORT = (byte) 2;
	public static final byte INT = (byte) 3;
	public static final byte LONG = (byte) 4;
	public static final byte FLOAT = (byte) 5;
	public static final byte DOUBLE = (byte) 6;
	public static final byte BYTE_ARRAY = (byte) 7;
	public static final byte STRING = (byte) 8;
	public static final byte LIST = (byte) 9;
	public static final byte COMPOUND = (byte) 10;
	public static final byte INT_ARRAY = (byte) 11;
	public static final byte LONG_ARRAY = (byte) 12;

}
