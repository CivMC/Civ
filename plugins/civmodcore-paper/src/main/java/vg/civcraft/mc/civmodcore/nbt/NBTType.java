package vg.civcraft.mc.civmodcore.nbt;

import lombok.experimental.UtilityClass;

/**
 * NBT Type IDs for usage with:
 *
 * <ul>
 *     <li>{@link net.minecraft.nbt.CompoundTag#hasKeyOfType(String, int)}</li>
 *     <li>{@link net.minecraft.nbt.Tag#getTypeId()}</li>
 *     <li>{@link net.minecraft.nbt.ListTag#e()} // list element type</li>
 *     <li>etc...</li>
 * </ul>
 *
 * This is a better version of {@link org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers.NBT}
 */
@UtilityClass
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
