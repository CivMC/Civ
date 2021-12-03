package vg.civcraft.mc.civmodcore.nbt.extensions;

import java.util.UUID;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;
import net.minecraft.nbt.NBTTagCompound;
import vg.civcraft.mc.civmodcore.utilities.UuidUtils;

/**
 * Set of extension methods for {@link NBTTagCompound}. Use {@link ExtensionMethod @ExtensionMethod} to take most
 * advantage of this.
 */
@UtilityClass
public final class NBTTagCompoundExtensions {

	private static final String UUID_MOST_SUFFIX = "Most";
	private static final String UUID_LEAST_SUFFIX = "Least";

	/**
	 * @param self The NBTTagCompound to get the size of.
	 * @return Returns the number of key-value pairs.
	 */
	public static int size(final NBTTagCompound self) {
		return self.x.size();
	}

	/**
	 * @param self The NBTTagCompound to clear.
	 */
	public static void clear(final NBTTagCompound self) {
		self.x.clear();
	}

	/**
	 * @param self The NBTTagCompound to do the adopting.
	 * @param other The NBTTagCompound to adopt.
	 */
	public static void adopt(final NBTTagCompound self,
							 final NBTTagCompound other) {
		if (self == other || self.x == other.x) { // Ignore highlighter
			return;
		}
		self.x.clear();
		self.x.putAll(other.x);
	}

	/**
	 * @param self The NBTTagCompound to check if there's a UUID on.
	 * @param key The key of the UUID.
	 * @return Returns true if there's a UUID value for that key.
	 */
	public static boolean hasUUID(final NBTTagCompound self,
								  final String key) {
		return self.b(key);
	}

	/**
	 * @param self The NBTTagCompound to get the UUID from.
	 * @param key The key of the UUID.
	 * @return Returns a UUID, defaulted to 00000000-0000-0000-0000-000000000000.
	 */
	public static UUID getUUID(final NBTTagCompound self,
							   final String key) {
		if (!hasUUID(self, key)) {
			return UuidUtils.IDENTITY;
		}
		return self.a(key);
	}

	/**
	 * @param self The NBTTagCompound to get the UUID from.
	 * @param key The key of the UUID.
	 * @param value The UUID value.
	 */
	public static void setUUID(final NBTTagCompound self,
							   final String key,
							   final UUID value) {
		setUUID(self, key, value, false);
	}

	/**
	 * @param self The NBTTagCompound to get the UUID from.
	 * @param key The key of the UUID.
	 * @param value The UUID value.
	 * @param useLegacyFormat Whether to use Mojang's legacy least+most format.
	 */
	public static void setUUID(final NBTTagCompound self,
							   final String key,
							   final UUID value,
							   final boolean useLegacyFormat) {
		if (value == null) {
			removeUUID(self, key);
			return;
		}
		if (useLegacyFormat) {
			self.a(key + UUID_MOST_SUFFIX, value.getMostSignificantBits());
			self.a(key + UUID_LEAST_SUFFIX, value.getLeastSignificantBits());
			return;
		}
		self.a(key, value);
	}

	/**
	 * @param self The NBTTagCompound to remove the UUID from.
	 * @param key The key of the UUID.
	 */
	public static void removeUUID(final NBTTagCompound self,
								  final String key) {
		self.r(key);
		self.r(key + UUID_MOST_SUFFIX);
		self.r(key + UUID_LEAST_SUFFIX);
	}

}
