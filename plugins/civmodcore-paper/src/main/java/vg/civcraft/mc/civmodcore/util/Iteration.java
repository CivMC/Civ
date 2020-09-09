package vg.civcraft.mc.civmodcore.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.apache.commons.lang.ArrayUtils;

public final class Iteration {

	@FunctionalInterface
	public interface ElementAndBoolConsumer<T> {
		void accept(T former, boolean latter);
	}

    /**
	 * <p><b>DO NOT USE THIS!</b></p>
	 *
	 * <p>This method was originally created for developer ease of use, but instead has become a hot bed for
	 * hidden bugs. The reason for that being the varargs / rest spread. If you put in an object you expect
	 * to be handled by {@link #isNullOrEmpty(Collection)} but don't realise the object doesn't actually
	 * inherit from {@link Collection}, such as {@link Map}, then it falls back to this version of the method,
	 * giving the illusion that it's doing what you intended. But instead of checking whether the object you
	 * gave is null or empty, it's instead only checking if it has any parameters.</p>
     *
	 * @deprecated Use {@link ArrayUtils#isEmpty(Object[])} instead.
     */
    @SafeVarargs
	@Deprecated
    public static <T> boolean isNullOrEmpty(T... array) {
		return ArrayUtils.isEmpty(array);
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
			return false;
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
	 * Iterates through a collection, whereby each element has knowledge of whether it's the last element.
	 *
	 * @param <T> The generic type of the collection.
	 * @param collection The collection to iterate.
	 * @param processor The iteration processor which will be called for each item in the collection.
	 */
    public static <T> void iterateHasNext(Collection<T> collection, ElementAndBoolConsumer<T> processor) {
		if (isNullOrEmpty(collection) || processor == null) {
			return;
		}
		Iterator<T> iterator = collection.iterator();
		while (iterator.hasNext()) {
			processor.accept(iterator.next(), iterator.hasNext());
		}
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

	/** @deprecated Use {@link #anyMatch(Object[], Predicate)} instead. */
	@Deprecated
	public static <T> boolean some(T[] array, Predicate<T> predicate) {
		return anyMatch(array, predicate);
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
	public static <T> boolean anyMatch(T[] array, Predicate<T> predicate) {
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

	/** @deprecated Use {@link #allMatch(Object[], Predicate)} instead. */
	@Deprecated
	public static <T> boolean every(T[] array, Predicate<T> predicate) {
		return allMatch(array, predicate);
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
	public static <T> boolean allMatch(T[] array, Predicate<T> predicate) {
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

	/**
	 * Removes the element at the end of the given list.
	 *
	 * @param <T> The type of the list's elements.
	 * @param list The list to remove the last element from.
	 * @return Returns the element removed.
	 */
	public static <T> T removeLastElement(List<T> list) {
		if (isNullOrEmpty(list)) {
			return null;
		}
		return list.remove(list.size() - 1);
	}

}
