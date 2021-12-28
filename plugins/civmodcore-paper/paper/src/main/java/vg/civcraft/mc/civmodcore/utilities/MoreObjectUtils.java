package vg.civcraft.mc.civmodcore.utilities;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ObjectUtils;

/**
 * Utility class that fills in the gaps of {@link ObjectUtils}.
 *
 * @author Protonull
 */
@UtilityClass
public final class MoreObjectUtils {

	/**
	 * Determines whether two things are equal based on their respective hash codes. This is obviously less accurate
	 * than {@link Object#equals(Object)}, however this can be used to, for example, test if an object has been changed.
	 *
	 * @param <L> The left hand object type.
	 * @param <R> The right hand object type.
	 * @param lhs The left hand object.
	 * @param rhs THe right hand object.
	 * @return Returns whether the left hand and right hand objects have matching hashes.
	 */
	public static <L, R> boolean hashEquals(final L lhs, final R rhs) {
		return lhs == rhs || (!(lhs == null || rhs == null) && lhs.hashCode() == rhs.hashCode());
	}

}
