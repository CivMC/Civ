package vg.civcraft.mc.civmodcore.nbt.extensions;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.utilities.UuidUtils;

/**
 * Set of extension methods for {@link net.minecraft.nbt.CompoundTag}.
 */
public final class NBTTagCompoundExtensions {

	private static final String UUID_MOST_SUFFIX = "Most";
	private static final String UUID_LEAST_SUFFIX = "Least";

	/**
	 * @param self The NBTTagCompound to get the size of.
	 * @return Returns the number of key-value pairs.
	 */
	public static int size(@NotNull final CompoundTag self) {
		return self.tags.size();
	}

	/**
	 * @param self The NBTTagCompound to clear.
	 */
	public static void clear(@NotNull final CompoundTag self) {
		self.tags.clear();
	}

	/**
	 * @param self The NBTTagCompound to do the adopting.
	 * @param other The NBTTagCompound to adopt.
	 */
	public static void adopt(@NotNull final CompoundTag self,
							 @NotNull final CompoundTag other) {
		if (self == other || self.tags == other.tags) { // Ignore highlighter
			return;
		}
		self.tags.clear();
		self.tags.putAll(other.tags);
	}

	/**
	 * @param self The NBTTagCompound to check if there's a UUID on.
	 * @param key The key of the UUID.
	 * @return Returns true if there's a UUID value for that key.
	 */
	public static boolean hasUUID(@NotNull final CompoundTag self,
								  final String key) {
		return self.hasUUID(key);
	}

	/**
	 * @param self The NBTTagCompound to get the UUID from.
	 * @param key The key of the UUID.
	 * @return Returns a UUID, defaulted to 00000000-0000-0000-0000-000000000000.
	 */
	@NotNull
	public static UUID getUUID(@NotNull final CompoundTag self,
							   final String key) {
		if (!hasUUID(self, key)) {
			return UuidUtils.IDENTITY;
		}
		return self.getUUID(key);
	}

	/**
	 * @param self The NBTTagCompound to get the UUID from.
	 * @param key The key of the UUID.
	 * @param value The UUID value.
	 */
	public static void setUUID(@NotNull final CompoundTag self,
							   @NotNull final String key,
							   final UUID value) {
		Objects.requireNonNull(key);
		setUUID(self, key, value, false);
	}

	/**
	 * @param self The NBTTagCompound to get the UUID from.
	 * @param key The key of the UUID.
	 * @param value The UUID value.
	 * @param useLegacyFormat Whether to use Mojang's legacy least+most format.
	 */
	public static void setUUID(@NotNull final CompoundTag self,
							   @NotNull final String key,
							   final UUID value,
							   final boolean useLegacyFormat) {
		Objects.requireNonNull(key);
		if (value == null) {
			removeUUID(self, key);
			return;
		}
		if (useLegacyFormat) {
			self.putLong(key + UUID_MOST_SUFFIX, value.getMostSignificantBits());
			self.putLong(key + UUID_LEAST_SUFFIX, value.getLeastSignificantBits());
			return;
		}
		self.putUUID(key, value);
	}

	/**
	 * @param self The NBTTagCompound to remove the UUID from.
	 * @param key The key of the UUID.
	 */
	public static void removeUUID(@NotNull final CompoundTag self,
								  @NotNull final String key) {
		Objects.requireNonNull(key);
		self.remove(key);
		self.remove(key + UUID_MOST_SUFFIX);
		self.remove(key + UUID_LEAST_SUFFIX);
	}

}
