package vg.civcraft.mc.civmodcore.utilities;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Utility class that fills in the gaps of {@link ArrayUtils}.
 *
 * @author Protonull
 */
@UtilityClass
public final class MoreArrayUtils {

    /**
     * Fills an array with a particular value.
     *
     * @param <T> The type of the array.
     * @param array The array to fill.
     * @param value The value to fill the array with.
     * @return Returns the given array with the filled values.
     */
    public static <T> T[] fill(final T[] array, final T value) {
        if (ArrayUtils.isEmpty(array)) {
            return array;
        }
        Arrays.fill(array, value);
        return array;
    }

    /**
     * <p>Tests whether there is at least one element in the given array that passes the criteria of the given
     * predicate.</p>
     *
     * <p>Emulates: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/some</p>
     *
     * @param <T> The type of the array's elements.
     * @param array The array to iterate.
     * @param predicate The element tester.
     * @return Returns true if at least one element passes the predicate test. Or false if the array fails the
     * {@link ArrayUtils#isEmpty(Object[]) isNullOrEmpty()} test, or true if the give predicate is null.
     */
    public static <T> boolean anyMatch(final T[] array, final Predicate<T> predicate) {
        if (ArrayUtils.isEmpty(array)) {
            return false;
        }
        if (predicate == null) {
            return true;
        }
        for (final T element : array) {
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
     * @param <T> The type of the array's elements.
     * @param array The array to iterate.
     * @param predicate The element tester.
     * @return Returns true if no element fails the predicate test, or if the array fails the
     * {@link ArrayUtils#isEmpty(Object[]) isNullOrEmpty()} test, or if the give predicate is null.
     */
    public static <T> boolean allMatch(final T[] array, final Predicate<T> predicate) {
        if (ArrayUtils.isEmpty(array)) {
            return true;
        }
        if (predicate == null) {
            return true;
        }
        for (final T element : array) {
            if (!predicate.test(element)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Attempts to retrieve an element from an array based on a given index. If the index is out of bounds, this
     * function will gracefully return fast, returning null.
     *
     * @param <T> The type of the array's elements.
     * @param array The array to get the element from.
     * @param index The index of the element.
     * @return Returns the element, or null.
     */
    public static <T> T getElement(final T[] array, final int index) {
        if (ArrayUtils.isEmpty(array) || index < 0 || index >= array.length) {
            return null;
        }
        return array[index];
    }

    /**
     * Retrieves a random element from an array of elements.
     *
     * @param <T> The type of element.
     * @param array The array to retrieve a value from.
     * @return Returns a random element, or null.
     */
    @SafeVarargs
    public static <T> T randomElement(final T... array) {
        if (ArrayUtils.isEmpty(array)) {
            return null;
        }
        if (array.length == 1) {
            return array[0];
        }
        return array[ThreadLocalRandom.current().nextInt(array.length)];
    }

    /**
	 * Computes elements, allowing them to be changed to something of the same type.
	 *
	 * @param <T> The type of element.
	 * @param array The array to compute the elements of.
	 * @param mapper The compute function itself.
	 */
    public static <T> void computeElements(final T[] array, final Function<T, T> mapper) {
    	if (ArrayUtils.isEmpty(array) || mapper == null) {
    		return;
		}
    	for (int i = 0, l = array.length; i < l; i++) {
			array[i] = mapper.apply(array[i]);
		}
	}

	/**
	 * Calculates the number of elements that fulfill a given condition.
	 *
	 * @param <T> The type of element.
	 * @param array The array to match the elements of.
	 * @param matcher The matcher function itself.
	 * @return Returns the number of elements that match.
	 */
	public static <T> int numberOfMatches(final T[] array, final Predicate<T> matcher) {
		if (ArrayUtils.isEmpty(array) || matcher == null) {
			return 0;
		}
		int counter = 0;
		for (final T element : array) {
			if (matcher.test(element)) {
				counter++;
			}
		}
		return counter;
	}

}
