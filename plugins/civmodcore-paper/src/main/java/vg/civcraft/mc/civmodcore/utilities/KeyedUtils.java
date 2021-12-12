package vg.civcraft.mc.civmodcore.utilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.experimental.UtilityClass;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

/**
 * Utility class to make dealing with namespace keys easier.
 */
@UtilityClass
public final class KeyedUtils {

	/**
	 * Converts a stringified namespaced-key back into a {@link NamespacedKey}.
	 *
	 * @param key The stringified namespace-key, which MUST be formatted as {@code namespace:name}
	 * @return Returns a valid {@link NamespacedKey}, or null.
	 *
	 * @exception IllegalArgumentException Will throw if the stringified key fails a "[a-z0-9._-]+" check, or if the
	 *            total length is longer than 256.
	 */
	@Nullable
	public static NamespacedKey fromString(@Nullable final String key) {
		return key == null ? null : NamespacedKey.fromString(key);
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
	@Nonnull
	public static NamespacedKey fromParts(@Nonnull final String namespace, @Nonnull final String key) {
		return new NamespacedKey(namespace, key);
	}

	/**
	 * Converts a {@link NamespacedKey} into a string.
	 *
	 * @param key The {@link NamespacedKey} to convert.
	 * @return Returns the stringified {@link NamespacedKey}, or null.
	 */
	@Nullable
	public static String getString(@Nullable final NamespacedKey key) {
		return key == null ? null : key.toString();
	}

	/**
	 * Retrieves a {@link Keyed}'s {@link NamespacedKey} and converts it to a string.
	 *
	 * @param keyed The {@link Keyed} instance.
	 * @return Returns the stringified {@link Keyed}'s {@link NamespacedKey}, or null.
	 */
	@Nullable
	public static String getString(@Nullable final Keyed keyed) {
		return keyed == null ? null : getString(keyed.getKey());
	}

	/**
	 * @param key The namespaced-key.
	 * @return Returns a new {@link NamespacedKey} for testing purposes.
	 */
	@SuppressWarnings("deprecation")
	@Nonnull
	public static NamespacedKey testKey(@Nonnull final String key) {
		return new NamespacedKey("test", key);
	}

}
