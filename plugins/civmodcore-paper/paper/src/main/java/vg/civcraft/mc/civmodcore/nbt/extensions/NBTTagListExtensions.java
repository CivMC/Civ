package vg.civcraft.mc.civmodcore.nbt.extensions;

import java.util.UUID;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.nbt.NBTType;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;
import vg.civcraft.mc.civmodcore.utilities.UuidUtils;

/**
 * Set of extension methods for {@link net.minecraft.nbt.ListTag}. Use {@link ExtensionMethod @ExtensionMethod} to take
 * most advantage of this.
 */
@UtilityClass
public final class NBTTagListExtensions {

	/**
	 * @param self The NBTTagList to get the element type of.
	 * @return Returns the NBTTagList's element type. Match it against {@link NBTType}. If it matches
	 *         {@link NBTType#END} then it's an empty list.
	 */
	public static byte getElementType(@NotNull final ListTag self) {
		return self.getElementType();
	}

	/**
	 * Checks whether a given NBT element is appropriate for the given list.
	 *
	 * @param self The NBTTagList to check against.
	 * @param value The value the check the appropriateness for.
	 * @return Returns true if the type is appropriate for the list.
	 */
	public static boolean isAppropriateType(@NotNull final ListTag self,
											final Tag value) {
		/** This is based off of {@link ListTag#updateType(Tag)} */
		if (value == null || value.getId() == NBTType.END) {
			return false;
		}
		final var elementType = getElementType(self);
		return elementType == NBTType.END || elementType == value.getId();
	}

	/**
	 * @param self The NBTTagList to get the boolean from.
	 * @param index The index of the boolean.
	 * @return Returns a boolean, defaulted to false.
	 */
	public static boolean getBoolean(@NotNull final ListTag self,
									 final int index) {
		return getByte(self, index) != (byte) 0;
	}

	/**
	 * @param self The NBTTagList to set the boolean to.
	 * @param index The index of the boolean to set.
	 * @param value The value of the boolean.
	 */
	public static void setBoolean(@NotNull final ListTag self,
								  final int index,
								  final boolean value) {
		self.set(index, ByteTag.valueOf(value));
	}

	/**
	 * @param self The NBTTagList to add the boolean to.
	 * @param index The index of the boolean to add to.
	 * @param value The value of the boolean.
	 */
	public static void addBoolean(@NotNull final ListTag self,
								  final int index,
								  final boolean value) {
		self.add(index, ByteTag.valueOf(value));
	}

	/**
	 * @param self The NBTTagList to add the boolean to.
	 * @param value The value of the boolean.
	 */
	public static void addBoolean(@NotNull final ListTag self,
								  final boolean value) {
		addElement(self, ByteTag.valueOf(value));
	}

	/**
	 * @param self The NBTTagList to get the byte from.
	 * @param index The index of the byte.
	 * @return Returns a byte, defaulted to 0x00.
	 */
	public static byte getByte(@NotNull final ListTag self,
							   final int index) {
		if (self.get(index) instanceof ByteTag nbtByte) {
			return nbtByte.getAsByte();
		}
		return (byte) 0;
	}

	/**
	 * @param self The NBTTagList to set the byte to.
	 * @param index The index of the byte to set.
	 * @param value The value of the byte.
	 */
	public static void setByte(@NotNull final ListTag self,
							   final int index,
							   final byte value) {
		self.set(index, ByteTag.valueOf(value));
	}

	/**
	 * @param self The NBTTagList to add the byte to.
	 * @param index The index of the byte to add to.
	 * @param value The value of the byte.
	 */
	public static void addByte(@NotNull final ListTag self,
							   final int index,
							   final byte value) {
		self.add(index, ByteTag.valueOf(value));
	}

	/**
	 * @param self The NBTTagList to add the boolean to.
	 * @param value The value of the boolean.
	 */
	public static void addByte(@NotNull final ListTag self,
							   final byte value) {
		addElement(self, ByteTag.valueOf(value));
	}

	/**
	 * @param self The NBTTagList to get the short from.
	 * @param index The index of the short.
	 * @return Returns a short, defaulted to 0.
	 */
	public static short getShort(@NotNull final ListTag self,
								 final int index) {
		return self.getShort(index);
	}

	/**
	 * @param self The NBTTagList to set the short to.
	 * @param index The index of the short to set.
	 * @param value The value of the short.
	 */
	public static void setShort(@NotNull final ListTag self,
								final int index,
								final short value) {
		self.set(index, ShortTag.valueOf(value));
	}

	/**
	 * @param self The NBTTagList to add the short to.
	 * @param index The index of the short to add to.
	 * @param value The value of the short.
	 */
	public static void addShort(@NotNull final ListTag self,
								final int index,
								final short value) {
		self.add(index, ShortTag.valueOf(value));
	}

	/**
	 * @param self The NBTTagList to add the short to.
	 * @param value The value of the short.
	 */
	public static void addShort(@NotNull final ListTag self,
								final short value) {
		addElement(self, ShortTag.valueOf(value));
	}

	/**
	 * @param self The NBTTagList to get the integer from.
	 * @param index The index of the integer.
	 * @return Returns an integer, defaulted to 0.
	 */
	public static int getInt(@NotNull final ListTag self,
							 final int index) {
		return self.getInt(index);
	}

	/**
	 * @param self The NBTTagList to set the integer to.
	 * @param index The index of the integer to set.
	 * @param value The value of the integer.
	 */
	public static void setInt(@NotNull final ListTag self,
							  final int index,
							  final int value) {
		self.set(index, IntTag.valueOf(value));
	}

	/**
	 * @param self The NBTTagList to add the integer to.
	 * @param index The index of the integer to add to.
	 * @param value The value of the integer.
	 */
	public static void addInt(@NotNull final ListTag self,
							  final int index,
							  final int value) {
		self.add(index, IntTag.valueOf(value));
	}

	/**
	 * @param self The NBTTagList to add the integer to.
	 * @param value The value of the integer.
	 */
	public static void addInt(@NotNull final ListTag self,
							  final int value) {
		addElement(self, IntTag.valueOf(value));
	}

	/**
	 * @param self The NBTTagList to get the long from.
	 * @param index The index of the long.
	 * @return Returns a long, defaulted to 0L.
	 */
	public static long getLong(@NotNull final ListTag self,
							   final int index) {
		if (self.get(index) instanceof LongTag nbtLong) {
			return nbtLong.getAsLong();
		}
		return 0L;
	}

	/**
	 * @param self The NBTTagList to set the long to.
	 * @param index The index of the long to set.
	 * @param value The value of the long.
	 */
	public static void setLong(@NotNull final ListTag self,
							   final int index,
							   final long value) {
		self.set(index, LongTag.valueOf(value));
	}

	/**
	 * @param self The NBTTagList to add the long to.
	 * @param index The index of the long to add to.
	 * @param value The value of the long.
	 */
	public static void addLong(@NotNull final ListTag self,
							   final int index,
							   final long value) {
		self.add(index, LongTag.valueOf(value));
	}

	/**
	 * @param self The NBTTagList to add the long to.
	 * @param value The value of the long.
	 */
	public static void addLong(@NotNull final ListTag self,
							   final long value) {
		addElement(self, LongTag.valueOf(value));
	}

	/**
	 * @param self The NBTTagList to get the float from.
	 * @param index The index of the float.
	 * @return Returns a float, defaulted to 0.0f.
	 */
	public static float getFloat(@NotNull final ListTag self,
								 final int index) {
		return self.getFloat(index);
	}

	/**
	 * @param self The NBTTagList to set the float to.
	 * @param index The index of the float to set.
	 * @param value The value of the float.
	 */
	public static void setFloat(@NotNull final ListTag self,
								final int index,
								final float value) {
		self.set(index, FloatTag.valueOf(value));
	}

	/**
	 * @param self The NBTTagList to add the float to.
	 * @param index The index of the float to add to.
	 * @param value The value of the float.
	 */
	public static void addFloat(@NotNull final ListTag self,
								final int index,
								final float value) {
		self.add(index, FloatTag.valueOf(value));
	}

	/**
	 * @param self The NBTTagList to add the float to.
	 * @param value The value of the float.
	 */
	public static void addFloat(@NotNull final ListTag self,
								final float value) {
		addElement(self, FloatTag.valueOf(value));
	}

	/**
	 * @param self The NBTTagList to get the double from.
	 * @param index The index of the double.
	 * @return Returns a double, defaulted to 0.0d.
	 */
	public static double getDouble(@NotNull final ListTag self,
								   final int index) {
		return self.getDouble(index);
	}

	/**
	 * @param self The NBTTagList to set the double to.
	 * @param index The index of the double to set.
	 * @param value The value of the double.
	 */
	public static void setDouble(@NotNull final ListTag self,
								 final int index,
								 final double value) {
		self.set(index, DoubleTag.valueOf(value));
	}

	/**
	 * @param self The NBTTagList to add the double to.
	 * @param index The index of the double to add to.
	 * @param value The value of the double.
	 */
	public static void addDouble(@NotNull final ListTag self,
								 final int index,
								 final double value) {
		self.add(index, DoubleTag.valueOf(value));
	}

	/**
	 * @param self The NBTTagList to add the double to.
	 * @param value The value of the double.
	 */
	public static void addDouble(@NotNull final ListTag self,
								 final double value) {
		addElement(self, DoubleTag.valueOf(value));
	}

	/**
	 * @param self The NBTTagList to get the UUID from.
	 * @param index The index of the UUID.
	 * @return Returns a UUID, defaulted to 00000000-0000-0000-0000-000000000000.
	 */
	public static UUID getUUID(@NotNull final ListTag self,
							   final int index) {
		final Tag value = self.get(index);
		if (value instanceof IntArrayTag nbtIntArray) {
			/** Copied from {@link net.minecraft.nbt.CompoundTag#getUUID(String)} */
			return NbtUtils.loadUUID(nbtIntArray);
		}
		if (value instanceof CompoundTag nbtCompound) {
			return NBTTagCompoundExtensions.getUUID(nbtCompound, NBTCompound.UUID_KEY);
		}
		return UuidUtils.IDENTITY;
	}

	/**
	 * @param self The NBTTagList to set the UUID to.
	 * @param index The index of the UUID to set.
	 * @param value The value of the UUID.
	 */
	public static void setUUID(@NotNull final ListTag self,
							   final int index,
							   final UUID value) {
		/** Copied from {@link CompoundTag#putUUID(String, UUID)} */
		self.set(index, NbtUtils.createUUID(value));
	}

	/**
	 * @param self The NBTTagList to add the UUID to.
	 * @param index The index of the UUID to add to.
	 * @param value The value of the UUID.
	 */
	public static void addUUID(@NotNull final ListTag self,
							   final int index,
							   final UUID value) {
		/** Copied from {@link CompoundTag#putUUID(String, UUID)} */
		self.add(index, NbtUtils.createUUID(value));
	}

	/**
	 * @param self The NBTTagList to add the UUID to.
	 * @param value The value of the UUID.
	 */
	public static void addUUID(@NotNull final ListTag self,
							   final UUID value) {
		/** Copied from {@link CompoundTag#putUUID(String, UUID)} */
		addElement(self, NbtUtils.createUUID(value));
	}

	/**
	 * @param self The NBTTagList to get the String from.
	 * @param index The index of the String.
	 * @return Returns a String, defaulted to "".
	 */
	public static String getString(@NotNull final ListTag self,
								   final int index) {
		if (self.get(index) instanceof StringTag nbtString) {
			return nbtString.getAsString();
		}
		return "";
	}

	/**
	 * @param self The NBTTagList to set the String to.
	 * @param index The index of the String to set.
	 * @param value The value of the String.
	 */
	public static void setString(@NotNull final ListTag self,
								 final int index,
								 final String value) {
		self.set(index, StringTag.valueOf(value));
	}

	/**
	 * @param self The NBTTagList to add the String to.
	 * @param index The index of the String to add to.
	 * @param value The value of the String.
	 */
	public static void addString(@NotNull final ListTag self,
								 final int index,
								 final String value) {
		self.add(index, StringTag.valueOf(value));
	}

	/**
	 * @param self The NBTTagList to add the String to.
	 * @param value The value of the String.
	 */
	public static void addString(@NotNull final ListTag self,
								 final String value) {
		addElement(self, StringTag.valueOf(value));
	}

	/**
	 * @param self The NBTTagList to get the compound from.
	 * @param index The index of the compound.
	 * @return Returns a compound, defaulted to {}.
	 */
	public static CompoundTag getCompound(@NotNull final ListTag self,
										  final int index) {
		if (self.get(index) instanceof CompoundTag nbtCompound) {
			return nbtCompound;
		}
		return new CompoundTag();
	}

	/**
	 * @param self The NBTTagList to set the compound to.
	 * @param index The index of the compound to set.
	 * @param value The value of the compound.
	 */
	public static void setCompound(@NotNull final ListTag self,
								   final int index,
								   final CompoundTag value) {
		self.set(index, value);
	}

	/**
	 * @param self The NBTTagList to add the compound to.
	 * @param index The index of the compound to add to.
	 * @param value The value of the compound.
	 */
	public static void addCompound(@NotNull final ListTag self,
								   final int index,
								   final CompoundTag value) {
		self.add(index, value);
	}

	/**
	 * @param self The NBTTagList to add the compound to.
	 * @param value The value of the compound.
	 */
	public static void addCompound(@NotNull final ListTag self,
								   final CompoundTag value) {
		addElement(self, value);
	}

	/**
	 * @param self The NBTTagList to get the integer array from.
	 * @param index The index of the integer array.
	 * @return Returns an integer array, defaulted to [].
	 */
	public static int[] getIntArray(@NotNull final ListTag self,
									final int index) {
		return self.getIntArray(index);
	}

	/**
	 * @param self The NBTTagList to set the integer array to.
	 * @param index The index of the integer array to set.
	 * @param values The value of the integer array.
	 */
	public static void setIntArray(@NotNull final ListTag self,
								   final int index,
								   final int[] values) {
		self.set(index, new IntArrayTag(values));
	}

	/**
	 * @param self The NBTTagList to add the integer array to.
	 * @param index The index of the integer array to add to.
	 * @param values The value of the integer array.
	 */
	public static void addIntArray(@NotNull final ListTag self,
								   final int index,
								   final int[] values) {
		self.add(index, new IntArrayTag(values));
	}

	/**
	 * @param self The NBTTagList to add the integer array to.
	 * @param values The value of the integer array.
	 */
	public static void addIntArray(@NotNull final ListTag self,
								   final int[] values) {
		addElement(self, new IntArrayTag(values));
	}

	/**
	 * @param self The NBTTagList to get the long array from.
	 * @param index The index of the long array.
	 * @return Returns an long array, defaulted to [].
	 */
	public static long[] getLongArray(@NotNull final ListTag self,
									  final int index) {
		return self.getLongArray(index);
	}

	/**
	 * @param self The NBTTagList to set the long array to.
	 * @param index The index of the long array to set.
	 * @param values The value of the long array.
	 */
	public static void setLongArray(@NotNull final ListTag self,
									final int index,
									final long[] values) {
		self.set(index, new LongArrayTag(values));
	}

	/**
	 * @param self The NBTTagList to add the long array to.
	 * @param index The index of the long array to add to.
	 * @param values The value of the long array.
	 */
	public static void addLongArray(@NotNull final ListTag self,
									final int index,
									final long[] values) {
		self.add(index, new LongArrayTag(values));
	}

	/**
	 * @param self The NBTTagList to add the long array to.
	 * @param values The value of the integer array.
	 */
	public static void addLongArray(@NotNull final ListTag self,
									final long[] values) {
		addElement(self, new LongArrayTag(values));
	}

	/**
	 * @param self The NBTTagList to get the list from.
	 * @param index The index of the list.
	 * @return Returns an list, defaulted to [].
	 */
	public static ListTag getList(@NotNull final ListTag self,
									 final int index) {
		return self.getList(index);
	}

	/**
	 * @param self The NBTTagList to set the list to.
	 * @param index The index of the list to set.
	 * @param value The value of the list.
	 */
	public static void setList(@NotNull final ListTag self,
							   final int index,
							   final ListTag value) {
		self.set(index, value);
	}

	/**
	 * @param self The NBTTagList to add the list to.
	 * @param index The index of the list to add to.
	 * @param value The value of the list.
	 */
	public static void addList(@NotNull final ListTag self,
							   final int index,
							   final ListTag value) {
		self.add(index, value);
	}

	/**
	 * @param self The NBTTagList to add the list to.
	 * @param value The value of the list.
	 */
	public static void addList(@NotNull final ListTag self,
							   final ListTag value) {
		addElement(self, value);
	}

	/**
	 * An alternative for {@link ListTag#add(Object)} for that respects type consistency.
	 *
	 * @param self The NBTTagList to add the list to.
	 * @param value The NBT element to add.
	 */
	public static void addElement(@NotNull final ListTag self,
								  @NotNull final Tag value) {
		if (!isAppropriateType(self, value)) {
			throw new UnsupportedOperationException(String.format(
					"Trying to add tag of type %d to list of %d",
					value.getId(), getElementType(self)));
		}
		self.add(value);
	}

}
