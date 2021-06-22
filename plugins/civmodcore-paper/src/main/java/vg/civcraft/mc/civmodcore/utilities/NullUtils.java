package vg.civcraft.mc.civmodcore.utilities;

import javax.annotation.Nonnull;

/**
 * @author Protonull
 */
public final class NullUtils {

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
    public static <T> T firstNonNull(final T... items) {
        for (final T item : items) {
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /**
     * Determines if two objects objects are equal, except that null values are disallowed.
     *
     * @param former The former object.
     * @param latter The latter object.
     * @return Returns true only if both objects are not null and pass an equals test.
     */
    public static boolean equalsNotNull(final Object former, final Object latter) {
        if (former == null || latter == null) {
            return false;
        }
        return former.equals(latter);
    }

    /**
	 * This is for when you're absolutely sure that the given object isn't null, but for some reason your IDE can't
	 * tell and is screaming at you to fix the null reference.
	 *
	 * @param <T> The type of the given object.
	 * @param object The object that isn't null.
	 * @return Returns the object highlighted as non-null.
	 */
    @Nonnull
    public static <T> T isNotNull(final T object) {
		return object;
	}

}
