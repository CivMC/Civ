package vg.civcraft.mc.civmodcore.utilities;

import org.apache.commons.lang3.StringUtils;

public final class MoreClassUtils {
	public static boolean isValidClassPath(
		final String path
	) {
		if (StringUtils.isBlank(path)) {
			return false;
		}
		for (final String segment : StringUtils.split(path, ".")) {
			if (segment.isBlank()) {
				return false;
			}
			if (!Character.isJavaIdentifierStart(segment.charAt(0))) {
				return false;
			}
			final int segmentLength = segment.length();
			if (segmentLength > 1) {
				for (int i = 1; i < segmentLength; i++) {
					if (!Character.isJavaIdentifierPart(segment.charAt(i))) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
