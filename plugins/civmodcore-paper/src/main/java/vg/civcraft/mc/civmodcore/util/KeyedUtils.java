package vg.civcraft.mc.civmodcore.util;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

/**
 * Utility class to make dealing with namespace keys easier.
 */
public final class KeyedUtils {

	/**
	 * Converts a stringified namespace back into a {@link NamespacedKey}.
	 *
	 * @param key The stringified namespace, which MUST be formatted as {@code namespace:name}
	 * @return Returns a valid {@link NamespacedKey}, or null.
	 *
	 * @exception IllegalArgumentException Will throw if the stringified key fails a "[a-z0-9._-]+" check, or if the
	 *     total length is longer than 256.
	 */
	public static NamespacedKey fromString(final String key) {
		if (Strings.isNullOrEmpty(key)) {
			return null;
		}
		final String[] parts = StringUtils.split(key, ":");
		if (parts.length != 2) {
			return null;
		}
		return fromParts(parts[0], parts[1]);
	}

	/**
	 * Converts a namespace and a key into a {@link NamespacedKey}.
	 *
	 * @param namespace The namespace name.
	 * @param key The namespaced key.
	 * @return Returns a valid {@link NamespacedKey}, or null.
	 *
	 * @exception IllegalArgumentException Will throw if either part fails a "[a-z0-9._-]+" check, or if the total
	 *     combined length is longer than 256.
	 */
	@SuppressWarnings("deprecation")
	public static NamespacedKey fromParts(final String namespace, final String key) {
		if (Strings.isNullOrEmpty(namespace) || Strings.isNullOrEmpty(key)) {
			return null;
		}
		return new NamespacedKey(namespace, key);
	}

	/**
	 * Converts a {@link NamespacedKey} into a string.
	 *
	 * @param key The {@link NamespacedKey} to convert.
	 * @return Returns the stringified {@link NamespacedKey}, or null.
	 */
	public static String getString(final NamespacedKey key) {
		if (key == null) {
			return null;
		}
		return key.toString();
	}

	/**
	 * Retrieves a {@link Keyed}'s {@link NamespacedKey} and converts it to a string.
	 *
	 * @param keyed The {@link Keyed} instance.
	 * @return Returns the stringified {@link Keyed}'s {@link NamespacedKey}, or null.
	 */
	public static String getString(final Keyed keyed) {
		if (keyed == null) {
			return null;
		}
		return getString(keyed.getKey());
	}

}
