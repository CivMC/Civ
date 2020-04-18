package vg.civcraft.mc.civmodcore.util;

import org.apache.commons.lang.NullArgumentException;

/**
 * Class for checking arguments and throwing null argument exceptions.
 *
 * @deprecated Just use {@link com.google.common.base.Preconditions Preconditions}.
 */
@Deprecated
public final class Guard {

	@Deprecated
	public static void ArgumentNotNull(Object argument, String parameterName) {
		if (parameterName == null) {
			throw new NullArgumentException("parameterName");
		}
		if (argument == null) {
			throw new NullArgumentException(parameterName);
		}
	}

	@Deprecated
	public static void ArgumentNotNullOrEmpty(String argument, String parameterName) {
		if (parameterName == null) {
			throw new NullArgumentException("parameterName");
		}
		if (argument == null) {
			throw new NullArgumentException(parameterName);
		}
		if (argument.equals("")) {
			throw new RuntimeException(parameterName + " can't be empty.");
		}
	}

	@Deprecated
	public static void ArgumentNotEquals(Object argument, String parameterName, Object other, String otherName) {
		if (argument.equals(other)) {
			throw new RuntimeException(parameterName + " can't be equal to " + otherName);
		}
	}

}
