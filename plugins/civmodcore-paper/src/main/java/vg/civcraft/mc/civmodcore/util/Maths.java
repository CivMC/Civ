package vg.civcraft.mc.civmodcore.util;

/**
 * General math utility class that <i>may</i> exist elsewhere, but I have none the foggiest where. If any of y'all
 * figure out where, feel free to lodge an official complaint
 * <a href="https://github.com/CivClassic/CivModCore/issues/new">here</a>.
 */
public class Maths {

	/**
	 * Limits a value between and including two values.
	 *
	 * @param value The value to clamp.
	 * @param min The minimum value.
	 * @param max The maximum value.
	 * @return The clamped value.
	 */
	public static int clamp(final int value, final int min, final int max) {
		return Math.max(min, Math.min(value, max));
	}

	/**
	 * Limits a value between and including two values.
	 *
	 * @param value The value to clamp.
	 * @param min The minimum value.
	 * @param max The maximum value.
	 * @return The clamped value.
	 */
	public static long clamp(final long value, final long min, final long max) {
		return Math.max(min, Math.min(value, max));
	}

	/**
	 * Limits a value between and including two values.
	 *
	 * @param value The value to clamp.
	 * @param min The minimum value.
	 * @param max The maximum value.
	 * @return The clamped value.
	 */
	public static float clamp(final float value, final float min, final float max) {
		return Math.max(min, Math.min(value, max));
	}

	/**
	 * Limits a value between and including two values.
	 *
	 * @param value The value to clamp.
	 * @param min The minimum value.
	 * @param max The maximum value.
	 * @return The clamped value.
	 */
	public static double clamp(final double value, final double min, final double max) {
		return Math.max(min, Math.min(value, max));
	}

	/**
	 * Normalises a value within a min-max range into a range between 0-1.
	 *
	 * @param value The value to normalise.
	 * @param min The min range value.
	 * @param max The max range value.
	 * @return The normalised value.
	 */
	public static int norm(final int value, final int min, final int max) {
		return (value - min) / (max - min);
	}

	/**
	 * Normalises a value within a min-max range into a range between 0-1.
	 *
	 * @param value The value to normalise.
	 * @param min The min range value.
	 * @param max The max range value.
	 * @return The normalised value.
	 */
	public static long norm(final long value, final long min, final long max) {
		return (value - min) / (max - min);
	}

	/**
	 * Normalises a value within a min-max range into a range between 0-1.
	 *
	 * @param value The value to normalise.
	 * @param min The min range value.
	 * @param max The max range value.
	 * @return The normalised value.
	 */
	public static float norm(final float value, final float min, final float max) {
		return (value - min) / (max - min);
	}

	/**
	 * Normalises a value within a min-max range into a range between 0-1.
	 *
	 * @param value The value to normalise.
	 * @param min The range's lower limit.
	 * @param max The range's upper limit.
	 * @return The normalised value.
	 */
	public static double norm(final double value, final double min, final double max) {
		return (value - min) / (max - min);
	}

	/**
	 * Applies a normalised value to a range.
	 *
	 * @param norm The {@link #norm(int, int, int) normalised} value.
	 * @param min The range's lower limit.
	 * @param max The range's upper limit.
	 * @return The linearly interpolated value.
	 */
	public static int lerp(final int norm, final int min, final int max) {
		return (max - min) * norm + min;
	}

	/**
	 * Applies a normalised value to a range.
	 *
	 * @param norm The {@link #norm(long, long, long) normalised} value.
	 * @param min The range's lower limit.
	 * @param max The range's upper limit.
	 * @return The linearly interpolated value.
	 */
	public static long lerp(final long norm, final long min, final long max) {
		return (max - min) * norm + min;
	}

	/**
	 * Applies a normalised value to a range.
	 *
	 * @param norm The {@link #norm(float, float, float) normalised} value.
	 * @param min The range's lower limit.
	 * @param max The range's upper limit.
	 * @return The linearly interpolated value.
	 */
	public static float lerp(final float norm, final float min, final float max) {
		return (max - min) * norm + min;
	}

	/**
	 * Applies a normalised value to a range.
	 *
	 * @param norm The {@link #norm(double, double, double) normalised} value.
	 * @param min The range's lower limit.
	 * @param max The range's upper limit.
	 * @return The linearly interpolated value.
	 */
	public static double lerp(final double norm, final double min, final double max) {
		return (max - min) * norm + min;
	}

	/**
	 * <p>This function will round numbers <i>away</i> from zero.</p>
	 *
	 * <ul>
	 *     <li>1.5 → 2</li>
	 *     <li>-1.5 → -2</li>
	 * </ul>
	 *
	 * @param value The value to round away from zero.
	 * @return The rounded value.
	 */
	public static double roundOut(final double value) {
		return value < 0 ? Math.floor(value) : Math.ceil(value);
	}

	/**
	 * <p>This function will round numbers <i>towards</i> zero.</p>
	 *
	 * <ul>
	 *     <li>1.5 → 1</li>
	 *     <li>-1.5 → -1</li>
	 * </ul>
	 *
	 * @param value The value to round towards zero.
	 * @return The rounded value.
	 */
	public static double roundIn(final double value) {
		return value < 0 ? Math.ceil(value) : Math.floor(value);
	}

}
