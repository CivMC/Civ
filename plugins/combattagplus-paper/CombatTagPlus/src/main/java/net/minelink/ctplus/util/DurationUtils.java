package net.minelink.ctplus.util;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.*;

public final class DurationUtils {

    public static String format(int seconds) {
        List<String> parts = new ArrayList<>();
        long days = seconds / MILLISECONDS.convert(1, DAYS);
        if (days > 0) {
            seconds -= MILLISECONDS.convert(days, DAYS);
            parts.add(days + (days > 1 ? " days" : " day"));
        }

        long hours = seconds / MILLISECONDS.convert(1, HOURS);
        if (hours > 0) {
            seconds -= MILLISECONDS.convert(hours, HOURS);
            parts.add(hours + (hours > 1 ? " hours" : " hour"));
        }

        long minutes = seconds / MILLISECONDS.convert(1, MINUTES);
        if (minutes > 0) {
            seconds -= MILLISECONDS.convert(minutes, MINUTES);
            parts.add(minutes + (minutes > 1 ? " minutes" : " minute"));
        }

        if (seconds > 0) {
            parts.add(seconds + (seconds > 1 ? " seconds" : " second"));
        }

        String formatted = StringUtils.join(parts, ", ");
        if (formatted.contains(", ")) {
            int index = formatted.lastIndexOf(", ");
            StringBuilder builder = new StringBuilder(formatted);
            formatted = builder.replace(index, index + 2, " and ").toString();
        }

        return formatted;
    }

}
