package vg.civcraft.mc.civmodcore.util;

import java.util.function.Consumer;

public final class NullCoalescing {

    @FunctionalInterface
    public interface NullChecker<T> {
        T get() throws Exception;
    }

    /**
     * Returns the first non-null given parameter, if any are given.
     *
     * @param <T> Any non-primitive type.
     * @param items The list of parameters to find a non-null value from.
     * @return Returns the first non-null value found, or null.
     *
     * @apiNote Attempts to fulfill the functionality of the Nullish coalescing operator in JavaScript.
     *         https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Nullish_coalescing_operator
     */
    @SafeVarargs
	public static <T> T coalesce(T... items) {
        for (T item : items) {
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /**
     * Allows developers to chain statements that might otherwise require a ton of null checking.
     *
     * @param <T> Any non-primitive type.
     * @param statement Function that throws an exception to call the chained statement within.
     * @return Returns the result of the chained statement, or null if the chain failed.
     *
     * @apiNote Attempts to fulfill the functionality of the Optional Chaining feature.
     *         https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Optional_chaining
     */
    public static <T> T chain(NullChecker<T> statement) {
        return chain(statement, null);
    }

    /**
     * Allows developers to chain statements that might otherwise require a ton of null checking.
     *
     * @param <T> Any non-primitive type.
     * @param statement Function that throws an exception to call the chained statement within.
     * @param fallback The value that will be fallen back upon if something goes wrong.
     * @return Returns the result of the chained statement, or the fallback if the chain failed.
     *
     * @apiNote Attempts to fulfill the functionality of the Optional Chaining feature matched together with the
     *         Nullish coalescing operator. Please use this only to retrieve nested values.
     *
     *         https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Optional_chaining
     *         https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Nullish_coalescing_operator
     */
    public static <T> T chain(NullChecker<T> statement, T fallback) {
        if (statement == null) {
            return fallback;
        }
        try {
            return statement.get();
        }
        catch (Exception ignored) {
            return fallback;
        }
    }

    /**
	 * Runs a handler only if the given value is not null.
	 *
	 * @param <T> The type of the given parameter.
	 * @param value The given parameter.
	 * @param handler The handler to run if the given parameter exists.
	 */
    public static <T> void exists(T value, Consumer<T> handler) {
    	if (value != null && handler != null) {
    		handler.accept(value);
		}
    }

}
