package vg.civcraft.mc.civmodcore.utilities;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.list.LazyList;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Utility class that fills in the gaps of {@link CollectionUtils}.
 *
 * @author Protonull
 */
public final class MoreCollectionUtils {

	/**
	 * Creates a new collection with a given set of predefined elements, if any are given.
	 *
	 * @param <T> The type of the elements to store in the collection.
	 * @param constructor The constructor for the collection.
	 * @param elements The elements to add to the collection.
	 * @return Returns a new collection, or null if no constructor was given, or the constructor didn't produce a new
	 * collection.
	 */
	@SafeVarargs
	public static <T, K extends Collection<T>> K collect(final Supplier<K> constructor, final T... elements) {
		if (constructor == null) {
			return null;
		}
		final K collection = constructor.get();
		if (collection == null) {
			return null;
		}
		CollectionUtils.addAll(collection, elements);
		return collection;
	}

	/**
	 * Creates a new collection with the exact size of a given set of predefined elements, if any are given.
	 *
	 * @param <T> The type of the elements to store in the collection.
	 * @param constructor The constructor for the collection.
	 * @param elements The elements to add to the collection.
	 * @return Returns a new collection, or null if no constructor was given, or the constructor didn't produce a new
	 * collection.
	 */
	@SafeVarargs
	public static <T, K extends Collection<T>> K collectExact(final Int2ObjectFunction<K> constructor,
															  final T... elements) {
		if (constructor == null) {
			return null;
		}
		if (elements == null) {
			return constructor.get(0);
		}
		final K collection = constructor.get(elements.length);
		if (collection != null) {
			Collections.addAll(collection, elements);
		}
		return collection;
	}

    /**
     * <p>Tests whether there is at least one element in the given collection that passes the criteria of the given
     * predicate.</p>
     *
     * <p>Emulates: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/some</p>
     *
     * @param <T> The type of the collection's elements.
     * @param collection The collection to iterate.
     * @param predicate The element tester.
     * @return Returns true if at least one element passes the predicate test. Or false if the array fails the
     * {@link ArrayUtils#isEmpty(Object[]) isNullOrEmpty()} test, or true if the give predicate is null.
     */
    public static <T> boolean anyMatch(final Collection<T> collection, final Predicate<T> predicate) {
        if (CollectionUtils.isEmpty(collection)) {
            return false;
        }
        if (predicate == null) {
            return true;
        }
        for (final T element : collection) {
            if (predicate.test(element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>Tests whether every element in an collection passes the criteria of the given predicate.</p>
     *
     * <p>Emulates: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/every</p>
     *
     * @param <T> The type of the collection's elements.
     * @param collection The collection to iterate.
     * @param predicate The element tester.
     * @return Returns true if no element fails the predicate test, or if the array fails the
     * {@link ArrayUtils#isEmpty(Object[]) isNullOrEmpty()} test, or if the give predicate is null.
     */
    public static <T> boolean allMatch(final Collection<T> collection, final Predicate<T> predicate) {
        if (CollectionUtils.isEmpty(collection)) {
            return true;
        }
        if (predicate == null) {
            return true;
        }
        for (final T element : collection) {
            if (!predicate.test(element)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes the element at the end of the given list.
     *
     * @param <T> The type of the list's elements.
     * @param list The list to remove the last element from.
     * @return Returns the element removed.
     */
    public static <T> T removeLastElement(final List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.remove(list.size() - 1);
    }

    /**
     * Retrieves a random element from an list of elements.
     *
     * @param <T> The type of element.
     * @param list The list to retrieve a value from.
     * @return Returns a random element, or null.
     */
    public static <T> T randomElement(final List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        final int size = list.size();
        if (size == 1) {
            return list.get(0);
        }
        return list.get(ThreadLocalRandom.current().nextInt(size));
    }

	/**
	 * Calculates the number of elements that fulfill a given condition.
	 *
	 * @param <T> The type of element.
	 * @param collection The collection to match the elements of.
	 * @param matcher The matcher function itself.
	 * @return Returns the number of elements that match.
	 */
	public static <T> int numberOfMatches(final Collection<T> collection, final Predicate<T> matcher) {
		if (CollectionUtils.isEmpty(collection) || matcher == null) {
			return 0;
		}
		int counter = 0;
		for (final T element : collection) {
			if (matcher.test(element)) {
				counter++;
			}
		}
		return counter;
	}

	public static <T> LazyList<T> lazyList(List<Supplier<T>> supplierList) {
		LazyList<T> lazyList = LazyList.lazyList(new ArrayList<>(supplierList.size()), i -> supplierList.get(i).get());
		// initialize size of LazyList if size > 0
		if (!supplierList.isEmpty()) {
			lazyList.get(supplierList.size() - 1);
		}
		return lazyList;
	}
}
