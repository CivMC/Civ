package sh.okx.railswitch.switches;

import com.google.common.base.Strings;
import java.util.Collection;
import java.util.List;

/**
 * Utility methods for splitting switch destinations into positive and negative buckets.
 */
public final class DestinationLists {
    /**
     * Splits an array of raw destination strings into positive and negative buckets.
     *
     * @param values    The values to split.
     * @param start     The first index to include from {@code values}.
     * @param positive  Collector for destinations that should be matched.
     * @param negative  Collector for destinations that should be excluded.
     */
    public static void splitDestinations(String[] values,
                                         int start,
                                         List<String> positive,
                                         List<String> negative) {
        if (values == null) {
            return;
        }
        int begin = Math.max(0, start);
        for (int index = begin; index < values.length; index++) {
            addDestination(values[index], positive, negative);
        }
    }

    /**
     * Splits a collection of raw destination strings into positive and negative buckets.
     *
     * @param values    The values to split.
     * @param positive  Collector for destinations that should be matched.
     * @param negative  Collector for destinations that should be excluded.
     */
    public static void splitDestinations(Iterable<String> values,
                                         List<String> positive,
                                         List<String> negative) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            addDestination(value, positive, negative);
        }
    }

    /**
     * Determines whether a target exists in a collection, ignoring case.
     *
     * @param values The collection to search.
     * @param target The value to locate.
     * @return {@code true} if {@code target} exists, {@code false} otherwise.
     */
    public static boolean containsIgnoreCase(Collection<String> values, String target) {
        if (Strings.isNullOrEmpty(target) || values == null) {
            return false;
        }
        for (String value : values) {
            if (target.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    private static void addDestination(String raw,
                                       List<String> positive,
                                       List<String> negative) {
        if (Strings.isNullOrEmpty(raw)) {
            return;
        }
        String value = raw.trim();
        if (value.isEmpty()) {
            return;
        }
        if (value.startsWith("!")) {
            String neg = value.substring(1).trim();
            if (!neg.isEmpty()) {
                negative.add(neg);
            }
            return;
        }
        positive.add(value);
    }
}
