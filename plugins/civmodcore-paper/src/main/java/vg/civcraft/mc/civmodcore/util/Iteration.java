package vg.civcraft.mc.civmodcore.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class Iteration {

    /**
     * Determines whether an array is null or empty.
     *
     * @param <T> The type of the array.
     * @param array The array to check.
     * @return Returns true if the array exists and at least one item.
     *
     * @apiNote This will not check the elements within the array. It only checks if the array itself exists and has
     *     elements. If for example the array has 100 null elements, this function would still return true.
     */
    @SafeVarargs
    public static <T> boolean isNullOrEmpty(T... array) {
        return array == null || array.length < 1;
    }

	/**
	 * Determines whether a collection is null or empty.
	 *
	 * @param <T> The type of collection.
	 * @param collection The collection to check.
	 * @return Returns true if the collection exists and at least one item.
	 *
	 * @apiNote This will not check the elements within the collection. It only checks if the collection itself exists
	 *     and has elements. If for example the collection has 100 null elements, this function would still return true.
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
     * @param base The baseline object to check against.
     * @param former The first of the two unknowns.
     * @param latter The second of the two unknowns.
     * @return Returns either the former or the latter parameter if either match, or null if neither do.
     *
     * @apiNote The base parameter, if being used to find the other block of a double chest, will be the location of the
     *     block that you got the Chest data from, and the two unknowns will be the left and right side.
     */
    public static <T> T other(T base, T former, T latter) {
        if (Objects.equals(base, former)) {
            return former;
        }
        if (Objects.equals(base, latter)) {
            return latter;
        }
        return null;
    }

    /**
	 * Tests whether there is at least one element in the given array that passes the criteria of the given predicate.
	 *
	 * @param <T> The type of the array elements.
	 * @param array The array to iterate.
	 * @param predicate The element tester.
	 * @return Returns true if at least one element passes the predicate test. Or false if the array fails the
	 * {@link Iteration#isNullOrEmpty(Object[]) isNullOrEmpty()} test, or true if the give predicate is null.
	 *
	 * @apiNote Made to function the similarly to:
	 *     https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/some
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
	 * Tests whether every element in an array passes the criteria of the given predicate.
	 *
	 * @param <T> The type of the array elements.
	 * @param array The array to iterate.
	 * @param predicate The element tester.
	 * @return Returns true if no element fails the predicate test, or if the array fails the
	 * {@link Iteration#isNullOrEmpty(Object[]) isNullOrEmpty()} test, or if the give predicate is null.
	 *
	 * @apiNote Made to function the similarly to:
	 *     https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/every
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

}
