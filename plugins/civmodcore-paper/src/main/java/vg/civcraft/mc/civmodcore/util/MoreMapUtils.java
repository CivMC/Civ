package vg.civcraft.mc.civmodcore.util;

import com.google.common.collect.BiMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Utility class that fills in the gaps of {@link MapUtils}.
 *
 * @author Protonull
 */
public final class MoreMapUtils {

    /**
     * Determines whether a Map Entry is valid in that it exists and so does the key and value.
     *
     * @param entry The map entry itself.
     * @return Returns true if the entry is considered valid.
     */
    public static boolean validEntry(final Map.Entry<?, ?> entry) {
        if (entry == null) {
            return false;
        }
        if (entry.getKey() == null) {
            return false;
        }
        if (entry.getValue() == null) {
            return false;
        }
        return true;
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
     */
    public static <K, V> K getKeyFromValue(final Map<K, V> map, final V value) {
        if (MapUtils.isEmpty(map)) {
            return null;
        }
        if (map instanceof BiMap) {
            return ((BiMap<K, V>) map).inverse().get(value);
        }
        for (final Map.Entry<K, V> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
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
     */
    @SafeVarargs
    public static <K, V, R> R attemptGet(final Map<K, V> map, final R fallback, final K... keys) {
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
    public static <K, V, R> R attemptGet(final Map<K, V> map,
                                         final Function<V, R> parser,
                                         final R fallback,
                                         final K... keys) {
        if (parser == null) {
            // Default parser (basic cast)
            return attemptGet(map, (V v) -> (R) v, fallback, keys);
        }
        if (MapUtils.isEmpty(map) || ArrayUtils.isEmpty(keys)) {
            return fallback;
        }
        for (final K key : keys) {
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

    /**
     * Sets all the given keys a particular value.
     *
     * @param <K> The type of the map's key.
     * @param <V> The type of the map's values.
     * @param map The map to set to.
     * @param value The value to set.
     * @param keys The keys to set the value for.
     */
    @SafeVarargs
    public static <K, V> void setMultipleKeys(final Map<K, V> map, final V value, final K... keys) {
        if (MapUtils.isEmpty(map) || ArrayUtils.isEmpty(keys)) {
            return;
        }
        for (final K key : keys) {
            map.put(key, value);
        }
    }

    /**
	 * @param <T> The type of the map's values.
	 * @return Returns a new TreeMap with a String keys that are <b>NOT</b> case sensitive.
	 */
    public static <T> TreeMap<String, T> newStringKeyMap() {
    	return new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	}

}
