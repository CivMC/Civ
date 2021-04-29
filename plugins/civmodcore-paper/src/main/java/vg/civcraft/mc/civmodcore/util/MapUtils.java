package vg.civcraft.mc.civmodcore.util;

import com.google.common.collect.BiMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Material;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;

/**
 * Class of Map utilities.
 *
 * @deprecated Use {@link MoreMapUtils} instead.
 */
@Deprecated
public final class MapUtils {

	/**
	 * <p>Determines whether a map is null or empty.</p>
	 *
	 * <p>Note: This will not check the elements within the map. It only checks if the map itself exists and has
	 * key-value pairs. If for example the map has 100 null keyed values, this function would still return true.</p>
	 *
	 * @param <K> The type of keys.
	 * @param <V> The type of values.
	 * @param map The map to check.
	 * @return Returns true if the map exists and at least one key-value pair.
	 *
	 * @deprecated Use {@link org.apache.commons.collections4.MapUtils#isEmpty(Map)} instead.
	 */
	@Deprecated
	public static <K, V> boolean isNullOrEmpty(Map<K, V> map) {
		return map == null || map.isEmpty();
	}

	/**
	 * Retrieves a key from a map based on a given value. If two or more keys share a value,
	 * the key that's returned is the first that matches during standard iteration.
	 *
	 * @param <K> The key type.
	 * @param <V> The value type.
	 * @param map The map to retrieve the key from.
	 * @param value The value to based the search on.
	 * @return Returns the key, or null.
	 *
	 * @deprecated Use {@link MoreMapUtils#getKeyFromValue(Map, Object)} instead.
	 */
	@Deprecated
	public static <K, V> K getKeyFromValue(final Map<K, V> map, final V value) {
		if (isNullOrEmpty(map)) {
			return null;
		}
		if (map instanceof BiMap) {
			return ((BiMap<K, V>) map).inverse().get(value);
		}
		for (final Map.Entry<K, V> entry : map.entrySet()) {
			if (NullCoalescing.equals(value, entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * Attempts to retrieve a value from a given map from a range of keys.
	 *
	 * @param <K> The key type of the map.
	 * @param <V> The value type of the map.
	 * @param <R> The desired return type.
	 * @param map The map to retrieve the value from.
	 * @param fallback The value that should be returned if none of the keys return a [valid] value.
	 * @param keys The keys to check.
	 * @return Returns a value, either from the keys or the fallback, both of which may be null.
	 *
	 * @deprecated Use {@link MoreMapUtils#attemptGet(Map, Object, Object[])} instead.
	 */
	@Deprecated
	@SafeVarargs
	public static <K, V, R> R attemptGet(Map<K, V> map, R fallback, K... keys) {
		return attemptGet(map, null, fallback, keys);
	}

	/**
	 * Attempts to retrieve a value from a given map from a range of keys.
	 *
	 * @param <K> The key type of the map.
	 * @param <V> The value type of the map.
	 * @param <R> The desired return type.
	 * @param map The map to retrieve the value from.
	 * @param parser The function to process the value from the map. Null will use a default parser.
	 * @param fallback The value that should be returned if none of the keys return a [valid] value.
	 * @param keys The keys to check.
	 * @return Returns a value, either from the keys or the fallback, both of which may be null.
	 *
	 * @deprecated Use {@link MoreMapUtils#attemptGet(Map, Function, Object, Object[])} instead.
	 */
	@Deprecated
	@SafeVarargs
	@SuppressWarnings("unchecked")
	public static <K, V, R> R attemptGet(Map<K, V> map, Function<V, R> parser, R fallback, K... keys) {
		if (isNullOrEmpty(map) || ArrayUtils.isEmpty(keys)) {
			return fallback;
		}
		if (parser == null) {
			// Default parser (basic cast)
			parser = (V v) -> (R) v;
		}
		for (K key : keys) {
			if (!map.containsKey(key)) {
				continue;
			}
			try {
				return parser.apply(map.get(key));
			}
			// Yeeeaaaah, I know this is a catch all exception and that's bad, but this really could be anything since
			// the parser function could be anything.. it could be a class cast, a null reference, number format...
			// But since this is a value parser and not an arbitrary code executor, nothing complication will be run,
			// so any exception cab be interpreted as a bad or unexpected value.
			catch (Exception ignored) {
				return fallback;
			}
		}
		return fallback;
	}

	// ------------------------------------------------------------
	// Parsers
	// ------------------------------------------------------------

	/**
	 * <p>Parses a list from a map.</p>
	 *
	 * <p>Use with {@link #attemptGet(Map, Function, Object, Object[])} as the parser.</p>
	 *
	 * @param value The value retrieved from the map.
	 * @return Returns the value cast to a list, or null.
	 */
	public static List<?> parseList(Object value) {
		if (value instanceof List) {
			return (List<?>) value;
		}
		return null;
	}

	/**
	 * <p>Parses a material from a map.</p>
	 *
	 * <p>Use with {@link #attemptGet(Map, Function, Object, Object[])} as the parser.</p>
	 *
	 * @param value The value retrieved from the map.
	 * @return Returns the value as a material, or null.
	 */
	public static Material parseMaterial(Object value) {
		if (value instanceof Material) {
			return (Material) value;
		}
		if (value instanceof String) {
			return MaterialUtils.getMaterial((String) value);
		}
		return null;
	}

}
