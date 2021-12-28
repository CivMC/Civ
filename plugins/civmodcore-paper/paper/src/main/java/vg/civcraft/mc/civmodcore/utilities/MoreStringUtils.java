package vg.civcraft.mc.civmodcore.utilities;

import java.util.List;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class MoreStringUtils {

	public static String joinOxfordComma(final String[] strings, final String comma, final String oxfordComma) {
		if (ArrayUtils.isEmpty(strings)) {
			return null;
		}
		return switch (strings.length) {
			case 1 -> strings[0];
			case 2 -> strings[0] + oxfordComma + strings[1];
			default -> StringUtils.join(ArrayUtils.subarray(strings, 0, strings.length - 1), comma) + oxfordComma + strings[strings.length - 1];
		};
	}

	public static String joinOxfordComma(final String[] strings) {
		return joinOxfordComma(strings, ", ", " and ");
	}

	public static String joinOxfordComma(final List<String> strings, final String comma, final String oxfordComma) {
		if (CollectionUtils.isEmpty(strings)) {
			return null;
		}
		return switch (strings.size()) {
			case 1 -> strings.get(0);
			case 2 -> strings.get(0) + oxfordComma + strings.get(1);
			default -> StringUtils.join(strings.subList(0, strings.size() - 1), comma) + oxfordComma + strings.get(strings.size() - 1);
		};
	}

	public static String joinOxfordComma(final List<String> strings) {
		return joinOxfordComma(strings, ", ", " and ");
	}

}
