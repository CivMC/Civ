package vg.civcraft.mc.civmodcore.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Iteration {

    /**
     * <p>Determines whether an array is null or empty.</p>
	 *
	 * <p>Note: This will not check the elements within the array. It only checks if the array itself exists and has
	 * elements. If for example the array has 100 null elements, this function would still return true.</p>
     *
     * @param <T> The type of the array.
     * @param array The array to check.
     * @return Returns true if the array exists and at least one item.
     */
    @SafeVarargs
    public static <T> boolean isNullOrEmpty(T... array) {
        return array == null || array.length < 1;
    }

	/**
	 * <p>Determines whether a collection is null or empty.</p>
	 *
	 * <p>Note: This will not check the elements within the collection. It only checks if the collection itself exists
	 * and has elements. If for example the collection has 100 null elements, this function would still return true.</p>
	 *
	 * @param <T> The type of collection.
	 * @param collection The collection to check.
	 * @return Returns true if the collection exists and at least one item.
	 */
    public static <T> boolean isNullOrEmpty(Collection<T> collection) {
    	return collection == null || collection.isEmpty();
	}

    /**
     * Returns the first matching item in the parameters, which is particularly useful when you need to match Materials
     * without necessarily needing to create a new {@link vg.civcraft.mc.civmodcore.api.MaterialAPI MaterialAPI}.
     *
     * @param <T> The type of the object to match.
     * @param base The object to match.
     * @param values An array of items to match against.
     * @return Returns true if the base is found within the values.
     */
    @SafeVarargs
    public static <T> boolean contains(T base, T... values) {
        if (isNullOrEmpty(values)) {
            return true;
        }
        for (T value : values) {
            if (Objects.equals(base, value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Iterates through a collection before clearing it completely. Useful for wiping out data on plugin disable.
     *
     * @param <T> The generic type of the collection.
     * @param collection The collection to iterate and clear.
     * @param processor The iteration processor which will be called for each item in the collection.
     */
    public static <T> void iterateThenClear(Collection<T> collection, Consumer<T> processor) {
    	if (isNullOrEmpty(collection) || processor == null) {
    		return;
		}
        for (T element : collection) {
            processor.accept(element);
        }
        collection.clear();
    }

    /**
	 * Fills an array with a particular value.
	 *
	 * @param <T> The type of the array.
	 * @param array The array to fill.
	 * @param value The value to fill the array with.
	 * @return Returns the given array with the filled values.
	 */
    public static <T> T[] fill(T[] array, T value) {
    	if (isNullOrEmpty(array)) {
    		return array;
		}
		Arrays.fill(array, value);
    	return array;
	}

    /**
     * Say you have three objects and you know two are the same but don't know which. This is useful when you're
     * attempting to find the other block of a double chest or a bed or a door, etc.
     *
     * @param <T> The type of the object to find the other of.
     * @param base The baseline object to check against, the known location.
     * @param former The first of the two unknowns.
     * @param latter The second of the two unknowns.
     * @return Returns either the former or the latter parameter if either match, or null if neither do.
     */
    public static <T> T other(T base, T former, T latter) {
        if (Objects.equals(base, former)) {
            return latter;
        }
        if (Objects.equals(base, latter)) {
            return former;
        }
        return null;
    }

    /**
	 * <p>Tests whether there is at least one element in the given array that passes the criteria of the given
	 * predicate.</p>
	 *
	 * <p>Emulates: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/some</p>
	 *
	 * @param <T> The type of the array elements.
	 * @param array The array to iterate.
	 * @param predicate The element tester.
	 * @return Returns true if at least one element passes the predicate test. Or false if the array fails the
	 * {@link Iteration#isNullOrEmpty(Object[]) isNullOrEmpty()} test, or true if the give predicate is null.
	 */
    public static <T> boolean some(T[] array, Predicate<T> predicate) {
    	if (isNullOrEmpty(array)) {
    		return false;
		}
    	if (predicate == null) {
    		return true;
		}
    	for (T element : array) {
    		if (predicate.test(element)) {
    			return true;
			}
		}
    	return false;
	}

	/**
	 * <p>Tests whether every element in an array passes the criteria of the given predicate.</p>
	 *
	 * <p>Emulates: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/every</p>
	 *
	 * @param <T> The type of the array elements.
	 * @param array The array to iterate.
	 * @param predicate The element tester.
	 * @return Returns true if no element fails the predicate test, or if the array fails the
	 * {@link Iteration#isNullOrEmpty(Object[]) isNullOrEmpty()} test, or if the give predicate is null.
	 */
	public static <T> boolean every(T[] array, Predicate<T> predicate) {
		if (isNullOrEmpty(array)) {
			return true;
		}
		if (predicate == null) {
			return true;
		}
		for (T element : array) {
			if (!predicate.test(element)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determines whether a Map Entry is valid in that it exists and so does the key and value.
	 *
	 * @param entry The map entry itself.
	 * @return Returns true if the entry is considered valid.
	 */
	public static boolean validEntry(Map.Entry<?, ?> entry) {
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
	 * Creates a new collection with a given set of predefined elements, if any are given.
	 *
	 * @param <T> The type of the elements to store in the collection.
	 * @param constructor The constructor for the collection.
	 * @param elements The elements to add to the collection.
	 * @return Returns a new collection, or null if no constructor was given, or the constructor didn't produce a new
	 *     collection.
	 */
	@SafeVarargs
	public static <T, K extends Collection<T>> K collect(Supplier<K> constructor, T... elements) {
		if (constructor == null) {
			return null;
		}
		K collection = constructor.get();
		if (collection == null) {
			return null;
		}
		if (isNullOrEmpty(elements)) {
			return collection;
		}
		for (T element : elements) {
			// Do not let this be simplified. There's no reason to create a new ArrayList
			// as it would be immediately discarded and that's... bad
			collection.add(element);
		}
		return collection;
	}

}
