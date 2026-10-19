package net.civmc.civproxy.sessionlimit;

import java.time.Duration;

final class DurationFormat {

    private DurationFormat() {
    }

    /**
     * 1:02:03 or 4:05, rounded up so the countdown never shows 0:00 while time remains
     */
    static String clock(final Duration duration) {
        final long seconds = ceilSeconds(duration);
        final long hours = seconds / 3600;
        final long minutes = (seconds % 3600) / 60;
        final long secs = seconds % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        }
        return String.format("%d:%02d", minutes, secs);
    }

    /**
     * "8 hours", "2 hours 5 minutes", "10 minutes", "30 seconds". Minutes are rounded up once there's at least one.
     */
    static String words(final Duration duration) {
        final long seconds = ceilSeconds(duration);
        if (seconds < 60) {
            return plural(seconds, "second");
        }
        final long totalMinutes = (seconds + 59) / 60;
        final long hours = totalMinutes / 60;
        final long minutes = totalMinutes % 60;
        if (hours == 0) {
            return plural(minutes, "minute");
        }
        if (minutes == 0) {
            return plural(hours, "hour");
        }
        return plural(hours, "hour") + " " + plural(minutes, "minute");
    }

    /**
     * "5h 12m", "45m", rounded down, for showing time already played
     */
    static String shortPlayed(final Duration duration) {
        final long totalMinutes = Math.max(0, duration.toMinutes());
        final long hours = totalMinutes / 60;
        final long minutes = totalMinutes % 60;
        return hours > 0 ? hours + "h " + minutes + "m" : minutes + "m";
    }

    private static long ceilSeconds(final Duration duration) {
        if (duration.isNegative()) {
            return 0;
        }
        return duration.getSeconds() + (duration.getNano() > 0 ? 1 : 0);
    }

    private static String plural(final long amount, final String unit) {
        return amount + " " + unit + (amount == 1 ? "" : "s");
    }
}
