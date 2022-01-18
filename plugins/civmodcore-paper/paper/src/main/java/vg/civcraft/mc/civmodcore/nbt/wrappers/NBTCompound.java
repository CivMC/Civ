package vg.civcraft.mc.civmodcore.nbt.wrappers;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.nbt.NBTType;
import vg.civcraft.mc.civmodcore.nbt.extensions.NBTTagListExtensions;
import vg.civcraft.mc.civmodcore.utilities.UuidUtils;

public class NBTCompound {

	public static final String NULL_STRING = "\u0000";
	public static final String UUID_KEY = "uuid";
	private static final String UUID_MOST_SUFFIX = "Most";
	private static final String UUID_LEAST_SUFFIX = "Least";

	private final CompoundTag tag;

	/**
	 * Creates a new NBTCompound.
	 */
	public NBTCompound() {
		this.tag = new CompoundTag();
	}

	/**
	 * Creates a new NBTCompound based on an existing inner-map.
	 */
	public NBTCompound(@NotNull final Map<String, Tag> raw) {
		this.tag = new CompoundTag(Objects.requireNonNull(raw)) {};
	}

	/**
	 * Creates a new NBTCompound by wrapping a given NMS compound.
	 *
	 * @param tag The NBTTagCompound to wrap.
	 */
	public NBTCompound(@NotNull final CompoundTag tag) {
		this.tag = Objects.requireNonNull(tag);
	}

	/**
	 * @return Returns the internal NMS compound.
	 */
	@NotNull
	public CompoundTag getRAW() {
		return this.tag;
	}

	/**
	 * Returns the size of the tag compound.
	 *
	 * @return The size of the tag compound.
	 */
	public int size() {
		return this.tag.tags.size();
	}

	/**
	 * Checks if the tag compound is empty.
	 *
	 * @return Returns true if the tag compound is empty.
	 */
	public boolean isEmpty() {
		return this.tag.tags.isEmpty();
	}

	/**
	 * Checks if the tag compound contains a particular key.
	 *
	 * @param key The key to check.
	 * @return Returns true if the contains the given key.
	 */
	public boolean hasKey(final String key) {
		return this.tag.tags.containsKey(key);
	}

	/**
	 * Checks if the tag compound contains a particular key of a particular type.
	 *
	 * @param key The key to check.
	 * @param type The type to check for.
	 * @return Returns true if the contains the given key of the given type.
	 */
	public boolean hasKeyOfType(@NotNull final String key,
								final int type) {
		return this.tag.contains(key, type);
	}

	/**
	 * Gets the keys within this compound.
	 *
	 * @return Returns the set of keys.
	 */
	@NotNull
	public Set<String> getKeys() {
		return this.tag.tags.keySet();
	}

	/**
	 * Moves a value from one key to another.
	 *
	 * @param fromKey The previous key.
	 * @param toKey The new key.
	 */
	public void switchKey(@NotNull final String fromKey,
						  @NotNull final String toKey) {
		if (StringUtils.equals(fromKey, toKey)) {
			return;
		}
		this.tag.tags.computeIfPresent(fromKey, (_key, value) -> {
			this.tag.tags.put(toKey, value);
			return null;
		});
	}

	/**
	 * <p>Removes a key and its respective value from the tag compound, if it exists.</p>
	 *
	 * <p>Note: If you're removing a UUID, use {@link NBTCompound#removeUUID(String)} instead.</p>
	 *
	 * @param key The key to remove.
	 */
	public void remove(final String key) {
		this.tag.tags.remove(key);
	}

	/**
	 * Clears all values from the tag compound.
	 */
	public void clear() {
		this.tag.tags.clear();
	}

	/**
	 * Adopts a copy of the NBT data from another compound.
	 *
	 * @param nbt The NBT data to copy and adopt.
	 */
	public void adopt(@NotNull final NBTCompound nbt) {
		Objects.requireNonNull(nbt);
		if (this == nbt || this.tag == nbt.tag) {
			return;
		}
		this.tag.tags.clear();
		this.tag.tags.putAll(nbt.tag.tags);
	}

	/**
	 * Gets a primitive boolean value from a key.
	 *
	 * @param key The key to get the boolean from.
	 * @return The value of the key, default: FALSE
	 */
	public boolean getBoolean(@NotNull final String key) {
		Objects.requireNonNull(key);
		return this.tag.getBoolean(key);
	}

	/**
	 * Sets a primitive boolean value to a key.
	 *
	 * @param key The key to set to the boolean to.
	 * @param value The boolean to set to the key.
	 */
	public void setBoolean(@NotNull final String key,
						   final boolean value) {
		Objects.requireNonNull(key);
		this.tag.putBoolean(key, value);
	}

	/**
	 * Gets a primitive byte value from a key.
	 *
	 * @param key The key to get the byte from.
	 * @return The value of the key, default: 0
	 */
	public byte getByte(@NotNull final String key) {
		Objects.requireNonNull(key);
		return this.tag.getByte(key);
	}

	/**
	 * Sets a primitive byte value to a key.
	 *
	 * @param key The key to set to the byte to.
	 * @param value The byte to set to the key.
	 */
	public void setByte(@NotNull final String key,
						final byte value) {
		Objects.requireNonNull(key);
		this.tag.putByte(key, value);
	}

	/**
	 * Gets a primitive short value from a key.
	 *
	 * @param key The key to get the short from.
	 * @return The value of the key, default: 0
	 */
	public short getShort(@NotNull final String key) {
		Objects.requireNonNull(key);
		return this.tag.getShort(key);
	}

	/**
	 * Sets a primitive short value to a key.
	 *
	 * @param key The key to set to the short to.
	 * @param value The short to set to the key.
	 */
	public void setShort(@NotNull final String key,
						 final short value) {
		Objects.requireNonNull(key);
		this.tag.putShort(key, value);
	}

	/**
	 * Gets a primitive integer value from a key.
	 *
	 * @param key The key to get the integer from.
	 * @return The value of the key, default: 0
	 */
	public int getInt(@NotNull final String key) {
		Objects.requireNonNull(key);
		return this.tag.getInt(key);
	}

	/**
	 * Sets a primitive integer value to a key.
	 *
	 * @param key The key to set to the integer to.
	 * @param value The integer to set to the key.
	 */
	public void setInt(@NotNull final String key,
					   final int value) {
		Objects.requireNonNull(key);
		this.tag.putInt(key, value);
	}

	/**
	 * Gets a primitive long value from a key.
	 *
	 * @param key The key to get the long from.
	 * @return The value of the key, default: 0L
	 */
	public long getLong(@NotNull final String key) {
		Objects.requireNonNull(key);
		return this.tag.getLong(key);
	}

	/**
	 * Sets a primitive long value to a key.
	 *
	 * @param key The key to set to the long to.
	 * @param value The long to set to the key.
	 */
	public void setLong(@NotNull final String key,
						final long value) {
		Objects.requireNonNull(key);
		this.tag.putLong(key, value);
	}

	/**
	 * Gets a primitive float value from a key.
	 *
	 * @param key The key to get the float from.
	 * @return The value of the key, default: 0f
	 */
	public float getFloat(@NotNull final String key) {
		Objects.requireNonNull(key);
		return this.tag.getFloat(key);
	}

	/**
	 * Sets a primitive float value to a key.
	 *
	 * @param key The key to set to the float to.
	 * @param value The float to set to the key.
	 */
	public void setFloat(@NotNull final String key,
						 final float value) {
		Objects.requireNonNull(key);
		this.tag.putFloat(key, value);
	}

	/**
	 * Gets a primitive double value from a key.
	 *
	 * @param key The key to get the double from.
	 * @return The value of the key, default: 0d
	 */
	public double getDouble(@NotNull final String key) {
		Objects.requireNonNull(key);
		return this.tag.getDouble(key);
	}

	/**
	 * Sets a primitive double value to a key.
	 *
	 * @param key The key to set to the double to.
	 * @param value The double to set to the key.
	 */
	public void setDouble(@NotNull final String key,
						  final double value) {
		Objects.requireNonNull(key);
		this.tag.putDouble(key, value);
	}

	/**
	 * Checks whether a UUID exists at the given key.
	 *
	 * @param key The key of the UUID.
	 * @return Returns true if a UUID exists at the given key.
	 */
	public boolean hasUUID(@NotNull final String key) {
		return this.tag.hasUUID(key);
	}

	/**
	 * Gets a UUID value from a key.
	 *
	 * @param key The key to get the UUID from.
	 * @return The value of the key, default: 00000000-0000-0000-0000-000000000000
	 */
	public UUID getUUID(@NotNull final String key) {
		Objects.requireNonNull(key);
		return !this.tag.hasUUID(key) ? UuidUtils.IDENTITY : this.tag.getUUID(key);
	}

	/**
	 * Gets a UUID value from a key.
	 *
	 * @param key The key to get the UUID from.
	 * @return The value of the key, default: NULL
	 */
	@Nullable
	public UUID getNullableUUID(@NotNull final String key) {
		Objects.requireNonNull(key);
		return !this.tag.hasUUID(key) ? null : this.tag.getUUID(key);
	}

	/**
	 * Sets a UUID value to a key.
	 *
	 * @param key The key to set to the UUID to.
	 * @param value The UUID to set to the key.
	 */
	public void setUUID(@NotNull final String key,
						final UUID value) {
		setUUID(key, value, false);
	}

	/**
	 * Sets a UUID value to a key.
	 *
	 * @param key The key to set to the UUID to.
	 * @param value The UUID to set to the key.
	 * @param useMojangFormat Whether to save as Mojang's least+most, or the updated int array.
	 */
	public void setUUID(@NotNull final String key,
						final UUID value,
						final boolean useMojangFormat) {
		Objects.requireNonNull(key);
		if (value == null) {
			removeUUID(key);
		}
		else {
			if (useMojangFormat) {
				this.tag.putLong(key + UUID_MOST_SUFFIX, value.getMostSignificantBits());
				this.tag.putLong(key + UUID_LEAST_SUFFIX, value.getLeastSignificantBits());
			}
			else {
				this.tag.putUUID(key, value);
			}
		}
	}

	/**
	 * Removes a UUID value, which is necessary because Bukkit stores UUIDs by splitting up the two significant parts
	 * into their own values.
	 *
	 * @param key The key of the UUID to remove.
	 */
	public void removeUUID(@NotNull final String key) {
		Objects.requireNonNull(key);
		remove(key);
		remove(key + UUID_MOST_SUFFIX);
		remove(key + UUID_LEAST_SUFFIX);
	}

	/**
	 * Gets a String value from a key.
	 *
	 * @param key The key to get the String from.
	 * @return The value of the key, default: ""
	 */
	@NotNull
	public String getString(@NotNull final String key) {
		Objects.requireNonNull(key);
		return this.tag.getString(key);
	}

	/**
	 * Gets a String value from a key.
	 *
	 * @param key The key to get the String from.
	 * @return The value of the key, default: NULL
	 */
	@Nullable
	public String getNullableString(@NotNull final String key) {
		Objects.requireNonNull(key);
		if (!this.tag.contains(key, 8)) {
			return null;
		}
		final String value = this.tag.getString(key);
		if (NULL_STRING.equals(value)) {
			return null;
		}
		return value;
	}

	/**
	 * Sets a String value to a key.
	 *
	 * @param key The key to set to the String to.
	 * @param value The String to set to the key.
	 */
	public void setString(@NotNull final String key,
						  final String value) {
		Objects.requireNonNull(key);
		if (value == null) {
			remove(key);
		}
		else {
			this.tag.putString(key, value);
		}
	}

	/**
	 * Gets an NBT compound from a key.
	 *
	 * @param key The key to get the NBT compound from.
	 * @return The value of the key, default: {}
	 */
	@NotNull
	public NBTCompound getCompound(@NotNull final String key) {
		final var found = getNullableCompound(key);
		return found == null ? new NBTCompound() : found;
	}

	/**
	 * Gets an NBT compound from a key.
	 *
	 * @param key The key to get the NBT compound from.
	 * @return The value of the key, default: NULL
	 */
	@Nullable
	public NBTCompound getNullableCompound(@NotNull final String key) {
		Objects.requireNonNull(key);
		final var found = this.tag.tags.get(key);
		if (found instanceof CompoundTag compound) {
			return new NBTCompound(compound);
		}
		return null;
	}

	/**
	 * Sets an NBT compound to a key.
	 *
	 * @param key The key to set to the NBT compound to.
	 * @param value The NBT compound to set to the key.
	 */
	public void setCompound(@NotNull final String key,
							final NBTCompound value) {
		Objects.requireNonNull(key);
		if (value == null) {
			remove(key);
		}
		else {
			this.tag.put(key, value.tag);
		}
	}

	/**
	 * Gets a Component value from a key.
	 *
	 * @param key The key of the Component.
	 * @return Returns a Component, defaulted to {@link Component#empty()}
	 */
	public Component getComponent(@NotNull final String key) {
		Objects.requireNonNull(key);
		if (hasKeyOfType(key, NBTType.STRING)) {
			return Component.empty();
		}
		else {
			return GsonComponentSerializer.gson().deserialize(getString(key));
		}
	}

	/**
	 * Sets a Component value to a key.
	 *
	 * @param key The key of the Component to set.
	 * @param value The Component value to set.
	 */
	public void setComponent(@NotNull final String key,
							 @NotNull final Component value) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(value);
		setString(key, GsonComponentSerializer.gson().serialize(value));
	}

	// ------------------------------------------------------------
	// Array Functions
	// ------------------------------------------------------------

	/**
	 * Gets an array of primitive booleans from a key.
	 *
	 * @param key The key to of the array.
	 * @return The values of the key, default: empty array
	 */
	@NotNull
	public boolean[] getBooleanArray(@NotNull final String key) {
		Objects.requireNonNull(key);
		final byte[] cache = getByteArray(key);
		final boolean[] result = new boolean[cache.length];
		for (int i = 0; i < cache.length; i++) {
			result[i] = cache[i] != 0;
		}
		return result;
	}

	/**
	 * Sets an array of primitive booleans to a key.
	 *
	 * @param key The key to set to array to.
	 * @param booleans The booleans to set to the key.
	 */
	public void setBooleanArray(@NotNull final String key,
								final boolean[] booleans) {
		Objects.requireNonNull(key);
		if (booleans == null) {
			remove(key);
			return;
		}
		final byte[] converted = new byte[booleans.length];
		for (int i = 0; i < booleans.length; i++) {
			converted[i] = (byte) (booleans[i] ? 1 : 0);
		}
		setByteArray(key, converted);
	}

	/**
	 * Gets an array of primitive bytes from a key.
	 *
	 * @param key The key to get the values of.
	 * @return The values of the key, default: empty array
	 */
	@NotNull
	public byte[] getByteArray(@NotNull final String key) {
		Objects.requireNonNull(key);
		return this.tag.getByteArray(key);
	}

	/**
	 * Sets an array of primitive bytes to a key.
	 *
	 * @param key The key to set to the bytes to.
	 * @param bytes The bytes to set to the key.
	 */
	public void setByteArray(@NotNull final String key,
							 final byte[] bytes) {
		Objects.requireNonNull(key);
		if (bytes == null) {
			remove(key);
			return;
		}
		this.tag.putByteArray(key, bytes);
	}

	/**
	 * Gets an array of primitive shorts from a key.
	 *
	 * @param key The key to get the values of.
	 * @return The values of the key, default: empty array
	 */
	@NotNull
	public short[] getShortArray(@NotNull final String key) {
		Objects.requireNonNull(key);
		final ListTag list = this.tag.getList(key, NBTType.SHORT);
		final short[] result = new short[list.size()];
		for (int i = 0; i < result.length; i++) {
			if (list.get(i) instanceof ShortTag nbtShort) {
				result[i] = nbtShort.getAsShort();
			}
		}
		return result;
	}

	/**
	 * Sets an array of primitive bytes to a key.
	 *
	 * @param key The key to set to values to.
	 * @param shorts The shorts to set to the key.
	 */
	public void setShortArray(@NotNull final String key,
							  final short[] shorts) {
		Objects.requireNonNull(key);
		if (shorts == null) {
			remove(key);
			return;
		}
		final ListTag list = new ListTag();
		for (final short value : shorts) {
			list.add(ShortTag.valueOf(value));
		}
		this.tag.put(key, list);
	}

	/**
	 * Gets an array of primitive integers from a key.
	 *
	 * @param key The key to get the values of.
	 * @return The values of the key, default: empty array
	 */
	@NotNull
	public int[] getIntArray(@NotNull final String key) {
		Objects.requireNonNull(key);
		return this.tag.getIntArray(key);
	}

	/**
	 * Sets an array of primitive integers to a key.
	 *
	 * @param key The key to set to values to.
	 * @param ints The values to set to the key.
	 */
	public void setIntArray(@NotNull final String key,
							final int[] ints) {
		Objects.requireNonNull(key);
		if (ints == null) {
			remove(key);
			return;
		}
		this.tag.putIntArray(key, ints);
	}

	/**
	 * Gets an array of primitive longs from a key.
	 *
	 * @param key The key to get the values of.
	 * @return The values of the key, default: empty array
	 */
	@NotNull
	public long[] getLongArray(@NotNull final String key) {
		Objects.requireNonNull(key);
		return this.tag.getLongArray(key);
	}

	/**
	 * Sets an array of primitive longs to a key.
	 *
	 * @param key The key to set to values to.
	 * @param longs The values to set to the key.
	 */
	public void setLongArray(@NotNull final String key,
							 final long[] longs) {
		Objects.requireNonNull(key);
		if (longs == null) {
			remove(key);
			return;
		}
		this.tag.putLongArray(key, longs);
	}

	/**
	 * Gets an array of primitive floats from a key.
	 *
	 * @param key The key to get the values of.
	 * @return The values of the key, default: empty array
	 */
	@NotNull
	public float[] getFloatArray(@NotNull final String key) {
		Objects.requireNonNull(key);
		final ListTag list = this.tag.getList(key, NBTType.FLOAT);
		final float[] result = new float[list.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = list.getFloat(i);
		}
		return result;
	}

	/**
	 * Sets an array of primitive floats to a key.
	 *
	 * @param key The key to set to values to.
	 * @param floats The values to set to the key.
	 */
	public void setFloatArray(@NotNull final String key,
							  final float[] floats) {
		Objects.requireNonNull(key);
		if (floats == null) {
			remove(key);
			return;
		}
		final ListTag list = new ListTag();
		for (final float value : floats) {
			list.add(FloatTag.valueOf(value));
		}
		this.tag.put(key, list);
	}

	/**
	 * Gets an array of primitive doubles from a key.
	 *
	 * @param key The key to get the values of.
	 * @return The values of the key, default: empty array
	 */
	@NotNull
	public double[] getDoubleArray(@NotNull final String key) {
		Objects.requireNonNull(key);
		final ListTag list = this.tag.getList(key, NBTType.DOUBLE);
		final double[] result = new double[list.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = list.getDouble(i);
		}
		return result;
	}

	/**
	 * Sets an array of primitive doubles to a key.
	 *
	 * @param key The key to set to values to.
	 * @param doubles The values to set to the key.
	 */
	public void setDoubleArray(@NotNull final String key,
							   final double[] doubles) {
		Objects.requireNonNull(key);
		if (doubles == null) {
			remove(key);
			return;
		}
		final ListTag list = new ListTag();
		for (final double value : doubles) {
			list.add(DoubleTag.valueOf(value));
		}
		this.tag.put(key, list);
	}

	/**
	 * Gets an array of UUIDs from a key.
	 *
	 * @param key The key to get the values of.
	 * @return The values of the key, default: empty array
	 */
	@NotNull
	public UUID[] getUUIDArray(@NotNull final String key) {
		Objects.requireNonNull(key);
		if (this.tag.get(key) instanceof ListTag list) {
			final UUID[] result = new UUID[list.size()];
			for (int i = 0, l = list.size(); i < l; i++) {
				result[i] = NBTTagListExtensions.getUUID(list, i);
			}
			return result;
		}
		return new UUID[0];
	}

	/**
	 * Sets an array of UUIDs to a key.
	 *
	 * @param key The key to set to values to.
	 * @param uuids The values to set to the key.
	 */
	public void setUUIDArray(@NotNull final String key,
							 final UUID[] uuids) {
		Objects.requireNonNull(key);
		if (uuids == null) {
			remove(key);
			return;
		}
		final ListTag list = new ListTag();
		for (final UUID value : uuids) {
			NBTTagListExtensions.addUUID(list, value);
		}
		this.tag.put(key, list);
	}

	/**
	 * Gets an array of Strings from a key.
	 *
	 * @param key The key to get the values of.
	 * @return The values of the key, default: empty array
	 */
	@NotNull
	public String[] getStringArray(@NotNull final String key) {
		Objects.requireNonNull(key);
		final ListTag list = this.tag.getList(key, NBTType.STRING);
		final String[] result = new String[list.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = list.get(i) instanceof StringTag nbtString ? nbtString.getAsString() : "";
		}
		return result;
	}

	/**
	 * Sets an array of Strings to a key.
	 *
	 * @param key The key to set to values to.
	 * @param strings The values to set to the key.
	 */
	public void setStringArray(@NotNull final String key,
							   final String[] strings) {
		Objects.requireNonNull(key);
		if (strings == null) {
			remove(key);
			return;
		}
		final ListTag list = new ListTag();
		List.of(strings).forEach((string) -> list.add(StringTag.valueOf(string)));
		this.tag.put(key, list);
	}

	/**
	 * Gets an array of tag compounds from a key.
	 *
	 * @param key The key to get the values of.
	 * @return The values of the key, default: empty array
	 */
	@NotNull
	public NBTCompound[] getCompoundArray(@NotNull final String key) {
		Objects.requireNonNull(key);
		final ListTag list = this.tag.getList(key, NBTType.COMPOUND);
		final NBTCompound[] result = new NBTCompound[list.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = new NBTCompound(list.getCompound(i));
		}
		return result;
	}

	/**
	 * Sets an array of tag compounds to a key.
	 *
	 * @param key The key to set to values to.
	 * @param compounds The values to set to the key.
	 */
	public void setCompoundArray(@NotNull final String key,
								 final NBTCompound[] compounds) {
		Objects.requireNonNull(key);
		if (compounds == null) {
			remove(key);
			return;
		}
		final ListTag list = new ListTag();
		list.addAll(Stream.of(compounds).map((nbt) -> nbt.tag).toList());
		this.tag.put(key, list);
	}

	// ------------------------------------------------------------
	// Object Overrides
	// ------------------------------------------------------------

	@Override
	public boolean equals(final Object object) {
		if (this == object) {
			return true;
		}
		if (object instanceof NBTCompound other) {
			return Objects.equals(this.tag.tags, other.tag.tags);
		}
		return false;
	}

	@NotNull
	@Override
	public String toString() {
		return "NBTCompound" + this.tag.tags;
	}

}
