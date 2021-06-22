package vg.civcraft.mc.civmodcore.nbt.extensions;

import java.util.UUID;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import vg.civcraft.mc.civmodcore.nbt.NBTType;
import vg.civcraft.mc.civmodcore.utilities.UuidUtils;

/**
 * Set of extension methods for {@link NBTTagList}. Use {@link ExtensionMethod @ExtensionMethod} to take most advantage
 * of this.
 */
@UtilityClass
public final class NBTTagListExtensions {

	/**
	 * @param self The NBTTagList to get the element type of.
	 * @return Returns the NBTTagList's element type. Match it against {@link NBTType}. If it matches
	 *         {@link NBTType#END} then it's an empty list.
	 */
	public static byte getElementType(final NBTTagList self) {
		return self.e();
	}

	/**
	 * Checks whether a given NBT element is appropriate for the given list.
	 *
	 * @param self The NBTTagList to check against.
	 * @param value The value the check the appropriateness for.
	 * @return Returns true if the type is appropriate for the list.
	 */
	public static boolean isAppropriateType(final NBTTagList self,
											final NBTBase value) {
		/** This is a direct copy of {@link NBTTagList#a(NBTBase)} */
		if (value == null || value.getTypeId() == NBTType.END) {
			return false;
		}
		final var elementType = getElementType(self);
		return elementType == NBTType.END || elementType == value.getTypeId();
	}

	/**
	 * @param self The NBTTagList to get the boolean from.
	 * @param index The index of the boolean.
	 * @return Returns a boolean, defaulted to false.
	 */
	public static boolean getBoolean(final NBTTagList self,
									 final int index) {
		return getByte(self, index) != (byte) 0;
	}

	/**
	 * @param self The NBTTagList to set the boolean to.
	 * @param index The index of the boolean to set.
	 * @param value The value of the boolean.
	 */
	public static void setBoolean(final NBTTagList self,
								  final int index,
								  final boolean value) {
		self.set(index, NBTTagByte.a(value));
	}

	/**
	 * @param self The NBTTagList to add the boolean to.
	 * @param index The index of the boolean to add to.
	 * @param value The value of the boolean.
	 */
	public static void addBoolean(final NBTTagList self,
								  final int index,
								  final boolean value) {
		self.add(index, NBTTagByte.a(value));
	}

	/**
	 * @param self The NBTTagList to add the boolean to.
	 * @param value The value of the boolean.
	 */
	public static void addBoolean(final NBTTagList self,
								  final boolean value) {
		addElement(self, NBTTagByte.a(value));
	}

	/**
	 * @param self The NBTTagList to get the byte from.
	 * @param index The index of the byte.
	 * @return Returns a byte, defaulted to 0x00.
	 */
	public static byte getByte(final NBTTagList self,
							   final int index) {
		if (self.get(index) instanceof NBTTagByte nbtByte) {
			return nbtByte.asByte();
		}
		return (byte) 0;
	}

	/**
	 * @param self The NBTTagList to set the byte to.
	 * @param index The index of the byte to set.
	 * @param value The value of the byte.
	 */
	public static void setByte(final NBTTagList self,
							   final int index,
							   final byte value) {
		self.set(index, NBTTagByte.a(value));
	}

	/**
	 * @param self The NBTTagList to add the byte to.
	 * @param index The index of the byte to add to.
	 * @param value The value of the byte.
	 */
	public static void addByte(final NBTTagList self,
							   final int index,
							   final byte value) {
		self.add(index, NBTTagByte.a(value));
	}

	/**
	 * @param self The NBTTagList to add the boolean to.
	 * @param value The value of the boolean.
	 */
	public static void addByte(final NBTTagList self,
							   final byte value) {
		addElement(self, NBTTagByte.a(value));
	}

	/**
	 * @param self The NBTTagList to get the short from.
	 * @param index The index of the short.
	 * @return Returns a short, defaulted to 0.
	 */
	public static short getShort(final NBTTagList self,
								 final int index) {
		return self.d(index);
	}

	/**
	 * @param self The NBTTagList to set the short to.
	 * @param index The index of the short to set.
	 * @param value The value of the short.
	 */
	public static void setShort(final NBTTagList self,
								final int index,
								final short value) {
		self.set(index, NBTTagShort.a(value));
	}

	/**
	 * @param self The NBTTagList to add the short to.
	 * @param index The index of the short to add to.
	 * @param value The value of the short.
	 */
	public static void addShort(final NBTTagList self,
								final int index,
								final short value) {
		self.add(index, NBTTagShort.a(value));
	}

	/**
	 * @param self The NBTTagList to add the short to.
	 * @param value The value of the short.
	 */
	public static void addShort(final NBTTagList self,
								final short value) {
		addElement(self, NBTTagShort.a(value));
	}

	/**
	 * @param self The NBTTagList to get the integer from.
	 * @param index The index of the integer.
	 * @return Returns an integer, defaulted to 0.
	 */
	public static int getInt(final NBTTagList self,
							 final int index) {
		return self.e(index);
	}

	/**
	 * @param self The NBTTagList to set the integer to.
	 * @param index The index of the integer to set.
	 * @param value The value of the integer.
	 */
	public static void setInt(final NBTTagList self,
							  final int index,
							  final int value) {
		self.set(index, NBTTagInt.a(value));
	}

	/**
	 * @param self The NBTTagList to add the integer to.
	 * @param index The index of the integer to add to.
	 * @param value The value of the integer.
	 */
	public static void addInt(final NBTTagList self,
							  final int index,
							  final int value) {
		self.add(index, NBTTagInt.a(value));
	}

	/**
	 * @param self The NBTTagList to add the integer to.
	 * @param value The value of the integer.
	 */
	public static void addInt(final NBTTagList self,
							  final int value) {
		addElement(self, NBTTagInt.a(value));
	}

	/**
	 * @param self The NBTTagList to get the long from.
	 * @param index The index of the long.
	 * @return Returns a long, defaulted to 0L.
	 */
	public static long getLong(final NBTTagList self,
							   final int index) {
		if (self.get(index) instanceof NBTTagLong nbtLong) {
			return nbtLong.asLong();
		}
		return 0L;
	}

	/**
	 * @param self The NBTTagList to set the long to.
	 * @param index The index of the long to set.
	 * @param value The value of the long.
	 */
	public static void setLong(final NBTTagList self,
							   final int index,
							   final long value) {
		self.set(index, NBTTagLong.a(value));
	}

	/**
	 * @param self The NBTTagList to add the long to.
	 * @param index The index of the long to add to.
	 * @param value The value of the long.
	 */
	public static void addLong(final NBTTagList self,
							   final int index,
							   final long value) {
		self.add(index, NBTTagLong.a(value));
	}

	/**
	 * @param self The NBTTagList to add the long to.
	 * @param value The value of the long.
	 */
	public static void addLong(final NBTTagList self,
							   final long value) {
		addElement(self, NBTTagLong.a(value));
	}

	/**
	 * @param self The NBTTagList to get the float from.
	 * @param index The index of the float.
	 * @return Returns a float, defaulted to 0.0f.
	 */
	public static float getFloat(final NBTTagList self,
								 final int index) {
		return self.i(index);
	}

	/**
	 * @param self The NBTTagList to set the float to.
	 * @param index The index of the float to set.
	 * @param value The value of the float.
	 */
	public static void setFloat(final NBTTagList self,
								final int index,
								final float value) {
		self.set(index, NBTTagFloat.a(value));
	}

	/**
	 * @param self The NBTTagList to add the float to.
	 * @param index The index of the float to add to.
	 * @param value The value of the float.
	 */
	public static void addFloat(final NBTTagList self,
								final int index,
								final float value) {
		self.add(index, NBTTagFloat.a(value));
	}

	/**
	 * @param self The NBTTagList to add the float to.
	 * @param value The value of the float.
	 */
	public static void addFloat(final NBTTagList self,
								final float value) {
		addElement(self, NBTTagFloat.a(value));
	}

	/**
	 * @param self The NBTTagList to get the double from.
	 * @param index The index of the double.
	 * @return Returns a double, defaulted to 0.0d.
	 */
	public static double getDouble(final NBTTagList self,
								   final int index) {
		return self.h(index);
	}

	/**
	 * @param self The NBTTagList to set the double to.
	 * @param index The index of the double to set.
	 * @param value The value of the double.
	 */
	public static void setDouble(final NBTTagList self,
								 final int index,
								 final double value) {
		self.set(index, NBTTagDouble.a(value));
	}

	/**
	 * @param self The NBTTagList to add the double to.
	 * @param index The index of the double to add to.
	 * @param value The value of the double.
	 */
	public static void addDouble(final NBTTagList self,
								 final int index,
								 final double value) {
		self.add(index, NBTTagDouble.a(value));
	}

	/**
	 * @param self The NBTTagList to add the double to.
	 * @param value The value of the double.
	 */
	public static void addDouble(final NBTTagList self,
								 final double value) {
		addElement(self, NBTTagDouble.a(value));
	}

	/**
	 * @param self The NBTTagList to get the UUID from.
	 * @param index The index of the UUID.
	 * @return Returns a UUID, defaulted to 00000000-0000-0000-0000-000000000000.
	 */
	public static UUID getUUID(final NBTTagList self,
							   final int index) {
		if (self.get(index) instanceof NBTTagIntArray nbtIntArray) {
			/** Copied from {@link NBTTagCompound#a(String)} */
			return GameProfileSerializer.a(nbtIntArray);
		}
		return UuidUtils.IDENTITY;
	}

	/**
	 * @param self The NBTTagList to set the UUID to.
	 * @param index The index of the UUID to set.
	 * @param value The value of the UUID.
	 */
	public static void setUUID(final NBTTagList self,
							   final int index,
							   final UUID value) {
		/** Copied from {@link NBTTagCompound#a(String, UUID)} */
		self.set(index, GameProfileSerializer.a(value));
	}

	/**
	 * @param self The NBTTagList to add the UUID to.
	 * @param index The index of the UUID to add to.
	 * @param value The value of the UUID.
	 */
	public static void addUUID(final NBTTagList self,
							   final int index,
							   final UUID value) {
		/** Copied from {@link NBTTagCompound#a(String, UUID)} */
		self.add(index, GameProfileSerializer.a(value));
	}

	/**
	 * @param self The NBTTagList to add the UUID to.
	 * @param value The value of the UUID.
	 */
	public static void addUUID(final NBTTagList self,
							   final UUID value) {
		/** Copied from {@link NBTTagCompound#a(String, UUID)} */
		addElement(self, GameProfileSerializer.a(value));
	}

	/**
	 * @param self The NBTTagList to get the String from.
	 * @param index The index of the String.
	 * @return Returns a String, defaulted to "".
	 */
	public static String getString(final NBTTagList self,
								   final int index) {
		if (self.get(index) instanceof NBTTagString nbtString) {
			return nbtString.asString();
		}
		return "";
	}

	/**
	 * @param self The NBTTagList to set the String to.
	 * @param index The index of the String to set.
	 * @param value The value of the String.
	 */
	public static void setString(final NBTTagList self,
								 final int index,
								 final String value) {
		self.set(index, NBTTagString.a(value));
	}

	/**
	 * @param self The NBTTagList to add the String to.
	 * @param index The index of the String to add to.
	 * @param value The value of the String.
	 */
	public static void addString(final NBTTagList self,
								 final int index,
								 final String value) {
		self.add(index, NBTTagString.a(value));
	}

	/**
	 * @param self The NBTTagList to add the String to.
	 * @param value The value of the String.
	 */
	public static void addString(final NBTTagList self,
								 final String value) {
		addElement(self, NBTTagString.a(value));
	}

	/**
	 * @param self The NBTTagList to get the compound from.
	 * @param index The index of the compound.
	 * @return Returns a compound, defaulted to {}.
	 */
	public static NBTTagCompound getCompound(final NBTTagList self,
											 final int index) {
		if (self.get(index) instanceof NBTTagCompound nbtCompound) {
			return nbtCompound;
		}
		return new NBTTagCompound();
	}

	/**
	 * @param self The NBTTagList to set the compound to.
	 * @param index The index of the compound to set.
	 * @param value The value of the compound.
	 */
	public static void setCompound(final NBTTagList self,
								   final int index,
								   final NBTTagCompound value) {
		self.set(index, value);
	}

	/**
	 * @param self The NBTTagList to add the compound to.
	 * @param index The index of the compound to add to.
	 * @param value The value of the compound.
	 */
	public static void addCompound(final NBTTagList self,
								   final int index,
								   final NBTTagCompound value) {
		self.add(index, value);
	}

	/**
	 * @param self The NBTTagList to add the compound to.
	 * @param value The value of the compound.
	 */
	public static void addCompound(final NBTTagList self,
								   final NBTTagCompound value) {
		addElement(self, value);
	}

	/**
	 * @param self The NBTTagList to get the integer array from.
	 * @param index The index of the integer array.
	 * @return Returns an integer array, defaulted to [].
	 */
	public static int[] getIntArray(final NBTTagList self,
									final int index) {
		return self.f(index);
	}

	/**
	 * @param self The NBTTagList to set the integer array to.
	 * @param index The index of the integer array to set.
	 * @param values The value of the integer array.
	 */
	public static void setIntArray(final NBTTagList self,
								   final int index,
								   final int[] values) {
		self.set(index, new NBTTagIntArray(values));
	}

	/**
	 * @param self The NBTTagList to add the integer array to.
	 * @param index The index of the integer array to add to.
	 * @param values The value of the integer array.
	 */
	public static void addIntArray(final NBTTagList self,
								   final int index,
								   final int[] values) {
		self.add(index, new NBTTagIntArray(values));
	}

	/**
	 * @param self The NBTTagList to add the integer array to.
	 * @param values The value of the integer array.
	 */
	public static void addIntArray(final NBTTagList self,
								   final int[] values) {
		addElement(self, new NBTTagIntArray(values));
	}

	/**
	 * @param self The NBTTagList to get the long array from.
	 * @param index The index of the long array.
	 * @return Returns an long array, defaulted to [].
	 */
	public static long[] getLongArray(final NBTTagList self,
									  final int index) {
		return self.g(index);
	}

	/**
	 * @param self The NBTTagList to set the long array to.
	 * @param index The index of the long array to set.
	 * @param values The value of the long array.
	 */
	public static void setLongArray(final NBTTagList self,
									final int index,
									final long[] values) {
		self.set(index, new NBTTagLongArray(values));
	}

	/**
	 * @param self The NBTTagList to add the long array to.
	 * @param index The index of the long array to add to.
	 * @param values The value of the long array.
	 */
	public static void addLongArray(final NBTTagList self,
									final int index,
									final long[] values) {
		self.add(index, new NBTTagLongArray(values));
	}

	/**
	 * @param self The NBTTagList to add the long array to.
	 * @param values The value of the integer array.
	 */
	public static void addLongArray(final NBTTagList self,
									final long[] values) {
		addElement(self, new NBTTagLongArray(values));
	}

	/**
	 * @param self The NBTTagList to get the list from.
	 * @param index The index of the list.
	 * @return Returns an list, defaulted to [].
	 */
	public static NBTTagList getList(final NBTTagList self,
									 final int index) {
		return self.b(index);
	}

	/**
	 * @param self The NBTTagList to set the list to.
	 * @param index The index of the list to set.
	 * @param value The value of the list.
	 */
	public static void setList(final NBTTagList self,
							   final int index,
							   final NBTTagList value) {
		self.set(index, value);
	}

	/**
	 * @param self The NBTTagList to add the list to.
	 * @param index The index of the list to add to.
	 * @param value The value of the list.
	 */
	public static void addList(final NBTTagList self,
							   final int index,
							   final NBTTagList value) {
		self.add(index, value);
	}

	/**
	 * @param self The NBTTagList to add the list to.
	 * @param value The value of the list.
	 */
	public static void addList(final NBTTagList self,
							   final NBTTagList value) {
		addElement(self, value);
	}

	/**
	 * An alternative for {@link NBTTagList#add(Object)} for that respects type consistency.
	 *
	 * @param self The NBTTagList to add the list to.
	 * @param value The NBT element to add.
	 */
	public static void addElement(final NBTTagList self,
								  final NBTBase value) {
		if (!isAppropriateType(self, value)) {
			throw new UnsupportedOperationException(String.format(
					"Trying to add tag of type %d to list of %d",
					value.getTypeId(), getElementType(self)));
		}
		self.add(value);
	}

}
