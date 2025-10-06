package xyz.huskydog.banstickCore.cmc.utils;

import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class to replace Bukkit's NumberConversions
 *
 * @implNote The methods in this class are meant to fail fast and hard if the input is invalid
 * @author Huskydog9988
 */
public class NumberUtils {

    public static int toInt(@Nullable Object object) {
        if (object == null) {
            return 0;
        } else if (object instanceof Number) {
            return ((Number) object).intValue();
        }

        return Integer.parseInt(object.toString());
    }

    public static long toLong(@Nullable Object object) {
        if (object == null) {
            return 0;
        } else if (object instanceof Number) {
            return ((Number) object).longValue();
        }

        return Long.parseLong(object.toString());
    }
}
