package vg.civcraft.mc.civmodcore.api;

import com.google.common.base.Preconditions;
import java.util.UUID;
import javax.annotation.Nonnull;
import net.minecraft.server.v1_14_R1.NBTBase;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import net.minecraft.server.v1_14_R1.NBTTagDouble;
import net.minecraft.server.v1_14_R1.NBTTagFloat;
import net.minecraft.server.v1_14_R1.NBTTagList;
import net.minecraft.server.v1_14_R1.NBTTagLong;
import net.minecraft.server.v1_14_R1.NBTTagShort;
import net.minecraft.server.v1_14_R1.NBTTagString;
import org.apache.commons.lang.StringUtils;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

/**
 * Wrapper class for NBTTagCompounds to make NBT serialisation and deserialisation as robust as possible. Intended to
 * replace {@link vg.civcraft.mc.civmodcore.itemHandling.TagManager TagManager} though the .mapToNBT and .listToNBT
 * APIs will not be re-implemented here as it's better to have a finer control of how data is written and read.
 * */
public class NBTCompound implements Cloneable {

	private NBTTagCompound tag;

	/**
	 * Creates a new NBTCompound, generating a new NBTTagCompound.
	 * */
	public NBTCompound() {
		this.tag = new NBTTagCompound();
	}

	/**
	 * Creates a new NBTCompound by wrapping an existing NBTTagCompound.
	 *
	 * @param tag The NBTTagCompound to wrap.
	 * */
	public NBTCompound(@Nonnull NBTTagCompound tag) {
		Preconditions.checkNotNull(tag, "Cannot create an NBTCompound from that tag; the tag is null.");
		this.tag = tag;
	}

	/**
	 * Returns the size of the tag compound.
	 *
	 * @return The size of tha tag compound.
	 * */
	public int size() {
		return this.tag.d();
	}

	/**
	 * Checks if the tag compound is empty.
	 *
	 * @return Returns true if the tag compound is empty.
	 * */
	public boolean isEmpty() {
		return this.tag.isEmpty();
	}

	/**
	 * Checks if the tag compound contains a particular key.
	 *
	 * @param key The key to check.
	 * @return Returns true if the contains the given key.
	 * */
	public boolean hasKey(String key) {
		return this.tag.hasKey(key);
	}

	/**
	 * Removes a key and its respective value from the tag compound, if it exists.
	 *
	 * @param key The key to remove.
	 * */
	public void remove(String key) {
		this.tag.remove(key);
	}

	/**
	 * Clears all values from the tag compound.
	 * */
	public void clear() {
		for (String key : this.tag.getKeys()) {
			this.tag.remove(key);
		}
	}

	/**
	 * Clones the NBT compound.
	 *
	 * @return Returns a duplicated version of this NBT compound.
	 * */
	@Override
	public NBTCompound clone() throws CloneNotSupportedException {
		NBTCompound clone = (NBTCompound) super.clone();
		clone.tag = this.tag.clone();
		return clone;
	}

	/**
	 * Returns the underlying NBTTagCompound.
	 *
	 * @return The wrapped NBTTagCompound.
	 * */
	public NBTTagCompound getRAW() {
		return this.tag;
	}

	/**
	 * Gets a primitive boolean value from a key.
	 *
	 * @param key The key to get the value of.
	 * @return The value of the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	public boolean getBoolean(@Nonnull String key) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key), "Cannot get that boolean; the key is null or empty.");
		return this.tag.getBoolean(key);
	}

	/**
	 * Sets a primitive boolean value to a key.
	 *
	 * @param key The key to set to value to.
	 * @param value The value to set to the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	public void setBoolean(@Nonnull String key, boolean value) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key), "Cannot set that boolean; the key is null or empty.");
		this.tag.setBoolean(key, value);
	}

	/**
	 * Gets an array of primitive booleans from a key.
	 *
	 * @param key The key to get the values of.
	 * @return The values of the key, which is never null.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	@Nonnull
	public boolean[] getBooleanArray(@Nonnull String key) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key),
				"Cannot get that boolean array; the key is null or empty.");
		byte[] cache = this.tag.getByteArray(key);
		boolean[] result = new boolean[cache.length];
		for (int i = 0; i < cache.length; i++) {
			result[i] = cache[i] == 0x1;
		}
		return result;
	}

	/**
	 * Sets an array of primitive booleans to a key.
	 *
	 * @param key The key to set to values to.
	 * @param values The values to set to the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty, or if the array is null.
	 * */
	public void setBooleanArray(@Nonnull String key, @Nonnull boolean[] values) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key),
				"Cannot set that boolean array; the key is null or empty.");
		Preconditions.checkNotNull(values,
				"Cannot set that boolean array; the array is null. Please use .remove(key) instead.");
		byte[] cache = new byte[values.length];
		for (int i = 0; i < values.length; i++) {
			cache[i] = (byte) (values[i] ? 0x1 : 0x0);
		}
		this.tag.setByteArray(key, cache);
	}

	/**
	 * Gets a primitive byte value from a key.
	 *
	 * @param key The key to get the value of.
	 * @return The value of the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	public byte getByte(@Nonnull String key) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key), "Cannot get that byte; the key is null or empty.");
		return this.tag.getByte(key);
	}

	/**
	 * Sets a primitive byte value to a key.
	 *
	 * @param key The key to set to value to.
	 * @param value The value to set to the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	public void setByte(@Nonnull String key, byte value) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key), "Cannot set that byte; the key is null or empty.");
		this.tag.setByte(key, value);
	}

	/**
	 * Gets an array of primitive bytes from a key.
	 *
	 * @param key The key to get the values of.
	 * @return The values of the key, which is never null.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	@Nonnull
	public byte[] getByteArray(@Nonnull String key) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key),
				"Cannot get that byte array; the key is null or empty.");
		return this.tag.getByteArray(key);
	}

	/**
	 * Sets an array of primitive bytes to a key.
	 *
	 * @param key The key to set to values to.
	 * @param values The values to set to the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty, or if the array is null.
	 * */
	public void setByteArray(@Nonnull String key, @Nonnull byte[] values) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key),
				"Cannot set that byte array; the key is null or empty.");
		Preconditions.checkNotNull(values,
				"Cannot set that byte array; the array is null. Please use .remove(key) instead.");
		this.tag.setByteArray(key, values);
	}

	/**
	 * Gets a primitive short value from a key.
	 *
	 * @param key The key to get the value of.
	 * @return The value of the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	public short getShort(@Nonnull String key) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key), "Cannot get that short; the key is null or empty.");
		return this.tag.getShort(key);
	}

	/**
	 * Sets a primitive short value to a key.
	 *
	 * @param key The key to set to value to.
	 * @param value The value to set to the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	public void setShort(@Nonnull String key, short value) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key), "Cannot set that short; the key is null or empty.");
		this.tag.setShort(key, value);
	}

	/**
	 * Gets an array of primitive shorts from a key.
	 *
	 * @param key The key to get the values of.
	 * @return The values of the key, which is never null.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	@Nonnull
	public short[] getShortArray(@Nonnull String key) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key),
				"Cannot get that short array; the key is null or empty.");
		NBTTagList list = this.tag.getList(key, 2);
		short[] result = new short[list.size()];
		for (int i = 0; i < result.length; i++) {
			NBTBase base = list.get(i);
			if (base.getTypeId() != 2) {
				result[i] = 0;
			}
			else if (!(base instanceof NBTTagShort)) {
				result[i] = 0;
			}
			else {
				result[i] = ((NBTTagShort) base).asShort();
			}
		}
		return result;
	}

	/**
	 * Sets an array of primitive bytes to a key.
	 *
	 * @param key The key to set to values to.
	 * @param values The values to set to the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty, or if the array is null.
	 * */
	public void setShortArray(@Nonnull String key, @Nonnull short[] values) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key),
				"Cannot set that short array; the key is null or empty.");
		Preconditions.checkNotNull(values,
				"Cannot set that short array; the array is null. Please use .remove(key) instead.");
		NBTTagList list = new NBTTagList();
		for (short value : values) {
			list.add(new NBTTagShort(value));
		}
		this.tag.set(key, list);
	}

	/**
	 * Gets a primitive integer value from a key.
	 *
	 * @param key The key to get the value of.
	 * @return The value of the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	public int getInteger(@Nonnull String key) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key), "Cannot get that integer; the key is null or empty.");
		return this.tag.getInt(key);
	}

	/**
	 * Sets a primitive integer value to a key.
	 *
	 * @param key The key to set to value to.
	 * @param value The value to set to the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	public void setInteger(@Nonnull String key, int value) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key), "Cannot set that integer; the key is null or empty.");
		this.tag.setInt(key, value);
	}

	/**
	 * Gets an array of primitive integers from a key.
	 *
	 * @param key The key to get the values of.
	 * @return The values of the key, which is never null.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	@Nonnull
	public int[] getIntegerArray(@Nonnull String key) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key),
				"Cannot get that integer array; the key is null or empty.");
		return this.tag.getIntArray(key);
	}

	/**
	 * Sets an array of primitive integers to a key.
	 *
	 * @param key The key to set to values to.
	 * @param values The values to set to the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty, or if the array is null.
	 * */
	public void setIntegerArray(@Nonnull String key, @Nonnull int[] values) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key),
				"Cannot set that integer array; the key is null or empty.");
		Preconditions.checkNotNull(values,
				"Cannot set that integer array; the array is null. Please use .remove(key) instead.");
		this.tag.setIntArray(key, values);
	}

	/**
	 * Gets a primitive long value from a key.
	 *
	 * @param key The key to get the value of.
	 * @return The value of the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	public long getLong(@Nonnull String key) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key), "Cannot get that long; the key is null or empty.");
		return this.tag.getLong(key);
	}

	/**
	 * Sets a primitive long value to a key.
	 *
	 * @param key The key to set to value to.
	 * @param value The value to set to the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	public void setLong(@Nonnull String key, long value) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key), "Cannot set that long; the key is null or empty.");
		this.tag.setLong(key, value);
	}

	/**
	 * Gets an array of primitive longs from a key.
	 *
	 * @param key The key to get the values of.
	 * @return The values of the key, which is never null.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	@Nonnull
	public long[] getLongArray(@Nonnull String key) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key),
				"Cannot get that long array; the key is null or empty.");
		NBTTagList list = this.tag.getList(key, 4);
		long[] result = new long[list.size()];
		for (int i = 0; i < result.length; i++) {
			NBTBase base = list.get(i);
			if (base.getTypeId() != 4) {
				result[i] = 0;
			}
			else if (!(base instanceof NBTTagLong)) {
				result[i] = 0;
			}
			else {
				result[i] = ((NBTTagLong) base).asLong();
			}
		}
		return result;
	}

	/**
	 * Sets an array of primitive longs to a key.
	 *
	 * @param key The key to set to values to.
	 * @param values The values to set to the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty, or if the array is null.
	 * */
	public void setLongArray(@Nonnull String key, @Nonnull long[] values) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key),
				"Cannot set that long array; the key is null or empty.");
		Preconditions.checkNotNull(values,
				"Cannot set that long array; the array is null. Please use .remove(key) instead.");
		NBTTagList list = new NBTTagList();
		for (long value : values) {
			list.add(new NBTTagLong(value));
		}
		this.tag.set(key, list);
	}

	/**
	 * Gets a primitive float value from a key.
	 *
	 * @param key The key to get the value of.
	 * @return The value of the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	public float getFloat(@Nonnull String key) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key),
				"Cannot get that float; the key is null or empty.");
		return this.tag.getFloat(key);
	}

	/**
	 * Sets a primitive float value to a key.
	 *
	 * @param key The key to set to value to.
	 * @param value The value to set to the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	public void setFloat(@Nonnull String key, float value) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key),
				"Cannot set that float; the key is null or empty.");
		this.tag.setFloat(key, value);
	}

	/**
	 * Gets an array of primitive floats from a key.
	 *
	 * @param key The key to get the values of.
	 * @return The values of the key, which is never null.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	@Nonnull
	public float[] getFloatArray(@Nonnull String key) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key),
				"Cannot get that float array; the key is null or empty.");
		NBTTagList list = this.tag.getList(key, 5);
		float[] result = new float[list.size()];
		for (int i = 0; i < result.length; i++) {
			NBTBase base = list.get(i);
			if (base.getTypeId() != 5) {
				result[i] = 0.0f;
			}
			else if (!(base instanceof NBTTagFloat)) {
				result[i] = 0.0f;
			}
			else {
				result[i] = ((NBTTagFloat) base).asFloat();
			}
		}
		return result;
	}

	/**
	 * Sets an array of primitive floats to a key.
	 *
	 * @param key The key to set to values to.
	 * @param values The values to set to the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty, or if the array is null.
	 * */
	public void setFloatArray(@Nonnull String key, @Nonnull float[] values) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key),
				"Cannot set that float array; the key is null or empty.");
		Preconditions.checkNotNull(values,
				"Cannot set that float array; the array is null. Please use .remove(key) instead.");
		NBTTagList list = new NBTTagList();
		for (float value : values) {
			list.add(new NBTTagFloat(value));
		}
		this.tag.set(key, list);
	}

	/**
	 * Gets a primitive double value from a key.
	 *
	 * @param key The key to get the value of.
	 * @return The value of the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	public double getDouble(@Nonnull String key) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key), "Cannot get that double; the key is null or empty.");
		return this.tag.getDouble(key);
	}

	/**
	 * Sets a primitive double value to a key.
	 *
	 * @param key The key to set to value to.
	 * @param value The value to set to the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	public void setDouble(@Nonnull String key, double value) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key), "Cannot set that double; the key is null or empty.");
		this.tag.setDouble(key, value);
	}

	/**
	 * Gets an array of primitive doubles from a key.
	 *
	 * @param key The key to get the values of.
	 * @return The values of the key, which is never null.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	@Nonnull
	public double[] getDoubleArray(@Nonnull String key) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key),
				"Cannot get that double array; the key is null or empty.");
		NBTTagList list = this.tag.getList(key, 6);
		double[] result = new double[list.size()];
		for (int i = 0; i < result.length; i++) {
			NBTBase base = list.get(i);
			if (base.getTypeId() != 6) {
				result[i] = 0.0d;
			}
			else if (!(base instanceof NBTTagDouble)) {
				result[i] = 0.0d;
			}
			else {
				result[i] = ((NBTTagDouble) base).asDouble();
			}
		}
		return result;
	}

	/**
	 * Sets an array of primitive doubles to a key.
	 *
	 * @param key The key to set to values to.
	 * @param values The values to set to the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty, or if the array is null.
	 * */
	public void setDoubleArray(@Nonnull String key, @Nonnull double[] values) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key),
				"Cannot set that double array; the key is null or empty.");
		Preconditions.checkNotNull(values,
				"Cannot set that double array; the array is null. Please use .remove(key) instead.");
		NBTTagList list = new NBTTagList();
		for (double value : values) {
			list.add(new NBTTagDouble(value));
		}
		this.tag.set(key, list);
	}

	/**
	 * Gets a UUID value from a key.
	 *
	 * @param key The key to get the value of.
	 * @return The value of the key, which is never null.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	@Nonnull
	public UUID getUUID(@Nonnull String key) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key), "Cannot get that UUID; the key is null or empty.");
		return this.tag.a(key);
	}

	/**
	 * Sets a UUID value to a key.
	 *
	 * @param key The key to set to value to.
	 * @param value The value to set to the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty, or if the value is null.
	 * */
	public void setUUID(@Nonnull String key, @Nonnull UUID value) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key), "Cannot set that UUID; the key is null or empty.");
		Preconditions.checkNotNull(value, "Cannot set that UUID; the value is null. Please use .remove(key) instead.");
		this.tag.a(key, value);
	}

	/**
	 * Gets a UUID value from a key.
	 *
	 * @param key The key to get the value of.
	 * @return The value of the key, which is never null.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	@Nonnull
	public String getString(@Nonnull String key) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key), "Cannot get that String; the key is null or empty.");
		return this.tag.getString(key);
	}

	/**
	 * Sets a String value to a key.
	 *
	 * @param key The key to set to value to.
	 * @param value The value to set to the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty, or if the value is null.
	 * */
	public void setString(@Nonnull String key, @Nonnull String value) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key), "Cannot set that String; the key is null or empty.");
		Preconditions.checkNotNull(value,
				"Cannot set that String; the value is null. Please use .remove(key) instead.");
		this.tag.setString(key, value);
	}

	/**
	 * Gets an array of Strings from a key.
	 *
	 * @param key The key to get the values of.
	 * @return The values of the key, which is never null.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	public String[] getStringArray(@Nonnull String key) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key),
				"Cannot get that String array; the key is null or empty.");
		NBTTagList list = this.tag.getList(key, 8);
		String[] result = new String[list.size()];
		for (int i = 0; i < result.length; i++) {
			NBTBase base = list.get(i);
			if (base.getTypeId() != 8) {
				result[i] = "";
			}
			else if (!(base instanceof NBTTagString)) {
				result[i] = "";
			}
			else {
				result[i] = ((NBTTagString) base).asString();
			}
		}
		return result;
	}

	/**
	 * Sets an array of Strings to a key.
	 *
	 * @param key The key to set to values to.
	 * @param values The values to set to the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty, or if the array is null.
	 * */
	public void setStringArray(@Nonnull String key, @Nonnull String[] values) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key),
				"Cannot set that String array; the key is null or empty.");
		Preconditions.checkNotNull(values,
				"Cannot set that String array; the array is null. Please use .remove(key) instead.");
		NBTTagList list = new NBTTagList();
		for (String value : values) {
			list.add(new NBTTagString(value));
		}
		this.tag.set(key, list);
	}

	/**
	 * Gets a tag compound value from a key.
	 *
	 * @param key The key to get the value of.
	 * @return The value of the key, which is never null.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	@Nonnull
	public NBTCompound getCompound(@Nonnull String key) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key),
				"Cannot get that tag compound; the key is null or empty.");
		return new NBTCompound(this.tag.getCompound(key));
	}

	/**
	 * Sets a tag compound value to a key.
	 *
	 * @param key The key to set to value to.
	 * @param value The value to set to the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty, or if the value is null.
	 * */
	public void setCompound(@Nonnull String key, @Nonnull NBTCompound value) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key),
				"Cannot set that tag compound; the key is null or empty.");
		Preconditions.checkNotNull(value,
				"Cannot set that tag compound; the value is null. Please use .remove(key) instead.");
		this.tag.set(key, value.tag);
	}

	/**
	 * Gets an array of tag compounds from a key.
	 *
	 * @param key The key to get the values of.
	 * @return The values of the key, which is never null.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty.
	 * */
	@Nonnull
	public NBTCompound[] getCompoundArray(@Nonnull String key) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key),
				"Cannot get that tag compound array; the key is null or empty.");
		NBTTagList list = this.tag.getList(key, 10);
		NBTCompound[] result = new NBTCompound[list.size()];
		for (int i = 0; i < result.length; i++) {
			NBTBase base = list.get(i);
			if (base.getTypeId() != 10) {
				result[i] = new NBTCompound();
			}
			else if (!(base instanceof NBTTagCompound)) {
				result[i] = new NBTCompound();
			}
			else {
				result[i] = new NBTCompound((NBTTagCompound) base);
			}
		}
		return result;
	}

	/**
	 * Sets an array of tag compounds to a key.
	 *
	 * @param key The key to set to values to.
	 * @param values The values to set to the key.
	 *
	 * @throws IllegalArgumentException Throws if the key is null or empty, or if the array is null.
	 * */
	public void setCompoundArray(@Nonnull String key, @Nonnull NBTCompound[] values) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(key),
				"Cannot set that tag compound array; the key is null or empty.");
		Preconditions.checkNotNull(values,
				"Cannot set that tag compound array; the array is null. Please use .remove(key) instead.");
		NBTTagList list = new NBTTagList();
		for (NBTCompound value : values) {
			list.add(value.tag);
		}
		this.tag.set(key, list);
	}

	/**
	 * Retrieves the NBT data from an item.
	 *
	 * @param item The item to retrieve the NBT form.
	 * @return Returns the item's NBT, which is never null.
	 *
	 * @throws IllegalArgumentException Throws if the item is null or if the NBT cannot be retrieved.
	 * */
	@Nonnull
	public static NBTCompound fromItem(@Nonnull ItemStack item) {
		Preconditions.checkNotNull(item, "Cannot create an NBTCompound from that item; the item is null.");
		net.minecraft.server.v1_14_R1.ItemStack craftItem = CraftItemStack.asNMSCopy(item);
		if (craftItem == null) {
			throw new IllegalArgumentException(
					"Cannot get the NBTCompound to that item; cannot convert to CraftItemStack.");
		}
		NBTTagCompound tag = craftItem.getTag();
		if (tag == null) {
			return new NBTCompound();
		}
		return new NBTCompound(tag);
	}

	/**
	 * Sets an NBT compound to an item. You must use the returned item, instead of the item you pass in.
	 *
	 * @param item The item to set the NBT to.
	 * @param nbt The NBT to set to the item.
	 * @return The item with the saved NBT.
	 *
	 * @throws IllegalArgumentException Throws if the item is null or if the NBT cannot be retrieved.
	 * */
	public static ItemStack toItem(@Nonnull ItemStack item, @Nonnull NBTCompound nbt) {
		Preconditions.checkNotNull(item, "Cannot set the NBTCompound to that item; the item is null.");
		Preconditions.checkNotNull(nbt, "Cannot set the NBTCompound to that item; the nbt is null.");
		net.minecraft.server.v1_14_R1.ItemStack craftItem = CraftItemStack.asNMSCopy(item);
		if (craftItem == null) {
			throw new IllegalArgumentException(
					"Cannot set the NBTCompound to that item; cannot convert to CraftItemStack.");
		}
		craftItem.setTag(nbt.tag);
		return CraftItemStack.asBukkitCopy(craftItem);
	}

}
