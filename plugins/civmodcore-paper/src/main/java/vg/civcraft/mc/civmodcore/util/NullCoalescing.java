package vg.civcraft.mc.civmodcore.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class NullCoalescing {

    @FunctionalInterface
    public interface NullChecker<T> {
        T get() throws Exception;
    }

    /**
     * <p>Returns the first non-null given parameter, if any are given.</p>
	 *
	 * <p>Emulates:
	 * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Nullish_coalescing_operator</p>
	 *
     * @param <T> Any non-primitive type.
     * @param items The list of parameters to find a non-null value from.
     * @return Returns the first non-null value found, or null.
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
     * <p>Allows developers to chain statements that might otherwise require a ton of null checking.</p>
     *
	 * <p>Emulates: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Optional_chaining</p>
	 *
     * @param <T> Any non-primitive type.
     * @param statement Function that throws an exception to call the chained statement within.
     * @return Returns the result of the chained statement, or null if the chain failed.
     */
    public static <T> T chain(NullChecker<T> statement) {
        return chain(statement, null);
    }

    /**
     * <p>Allows developers to chain statements that might otherwise require a ton of null checking.</p>
	 *
	 * <p>Emulates: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Optional_chaining and
	 * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Nullish_coalescing_operator</p>
	 *
     * @param <T> Any non-primitive type.
     * @param statement Function that throws an exception to call the chained statement within.
     * @param fallback The value that will be fallen back upon if something goes wrong.
     * @return Returns the result of the chained statement, or the fallback if the chain failed.
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

    /**
	 * Executes a function to supply a value should that value not already exist.
	 *
	 * @param <T> The type of the value.
	 * @param value The given value.
	 * @param handler The supplier that will be run should the given value be null.
	 * @return Returns the given value or the result of the handler.
	 */
    public static <T> T notExists(T value, Supplier<T> handler) {
    	if (value == null && handler != null) {
    		value = handler.get();
		}
    	return value;
	}

	/**
	 * Checks whether a value can be cast to a particular type.
	 *
	 * @param <T> The type to cast to.
	 * @param clazz The class of the type.
	 * @param value The value to attempt to cast.
	 * @return Returns the value cast to the given type, nor null.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T castOrNull(Class<T> clazz, Object value) {
		if (clazz == null || value == null) {
			return null;
		}
		if (clazz.isAssignableFrom(value.getClass())) {
			return (T) value;
		}
		return null;
	}

	/**
	 * Determines if two objects objects are equal.
	 *
	 * @param former The former object.
	 * @param latter The latter object.
	 * @return Returns true only if both objects are not null and pass an equals test.
	 */
	public static boolean equals(Object former, Object latter) {
		if (former != null && former.equals(latter)) {
			return true;
		}
		if (latter != null && latter.equals(former)) {
			return true;
		}
		return false;
	}

    /**
	 * Determines if two objects objects are equal, except that null values are disallowed.
	 *
	 * @param former The former object.
	 * @param latter The latter object.
	 * @return Returns true only if both objects are not null and pass an equals test.
	 */
    public static boolean equalsNotNull(Object former, Object latter) {
    	if (former == null || latter == null) {
    		return false;
		}
    	if (former.equals(latter)) {
    		return true;
		}
    	if (latter.equals(former)) {
    		return true;
		}
    	return false;
	}

}
