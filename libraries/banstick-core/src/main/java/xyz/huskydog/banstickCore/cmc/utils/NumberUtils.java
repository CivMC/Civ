package xyz.huskydog.banstickCore.cmc.utils;

import org.jetbrains.annotations.Nullable;

/**
 * Utility class to replace Bukkit's NumberConversions
 *
 * @author Huskydog9988
 * @implNote The methods in this class are meant to fail fast and hard if the input is invalid
 */
public interface NumberUtils {

    /**
     * Convert an object to an int
     * @param object to convert
     * @return int value or 0 if null
     * @throws NumberFormatException if the object cannot be converted to an int
     */
    static int toInt(@Nullable Object object) {
        if (object == null) {
            return 0;
        } else if (object instanceof Number) {
            return ((Number) object).intValue();
        }

        return Integer.parseInt(object.toString());
    }

    /**
     * Convert an object to a long
     * @param object to convert
     * @return long value or 0 if null
     * @throws NumberFormatException if the object cannot be converted to a long
     */
    static long toLong(@Nullable Object object) {
        if (object == null) {
            return 0;
        } else if (object instanceof Number) {
            return ((Number) object).longValue();
        }

        return Long.parseLong(object.toString());
    }
}
