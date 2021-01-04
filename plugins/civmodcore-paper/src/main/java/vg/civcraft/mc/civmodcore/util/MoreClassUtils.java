package vg.civcraft.mc.civmodcore.util;

public final class MoreClassUtils {

	/**
	 * Checks whether a value can be cast to a particular type.
	 *
	 * @param <T>   The type to cast to.
	 * @param clazz The class of the type.
	 * @param value The value to attempt to cast.
	 * @return Returns the value cast to the given type, nor null.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T castOrNull(final Class<T> clazz, final Object value) {
		if (clazz == null || value == null) {
			return null;
		}
		if (clazz.isAssignableFrom(value.getClass())) {
			return (T) value;
		}
		return null;
	}





}
