package vg.civcraft.mc.civmodcore.util;

import java.util.Map;
import java.util.function.Function;

/**
 * Class of Map utilities.
 */
public final class MapUtils {

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
	 */
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
	 */
	@SafeVarargs
	@SuppressWarnings("unchecked")
	public static <K, V, R> R attemptGet(Map<K, V> map, Function<V, R> parser, R fallback, K... keys) {
		if (Iteration.isNullOrEmpty(map) || Iteration.isNullOrEmpty(keys)) {
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

}
