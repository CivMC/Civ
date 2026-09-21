package net.civmc.civproxy.sessionlimit;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public record SessionLimitConfig(
    boolean enabled,
    boolean dryRun,
    String server,
    Duration limit,
    Duration warning,
    Duration breakReset,
    Duration queueGrace,
    List<Duration> chatWarnings
) {

    private static final List<Double> DEFAULT_CHAT_WARNINGS = List.of(60D, 30D, 15D, 5D, 1D);

    /**
     * A missing section loads as disabled, so existing configs keep working unchanged.
     */
    public static SessionLimitConfig load(final ConfigurationNode node) {
        final Duration limit = minutes(node.node("limit-minutes").getDouble(480), Duration.ofMinutes(1));
        final Duration warning = minutes(node.node("warning-minutes").getDouble(60), Duration.ZERO);

        List<Double> warningMinutes;
        try {
            warningMinutes = node.node("chat-warnings-minutes").getList(Double.class, DEFAULT_CHAT_WARNINGS);
        } catch (final SerializationException exception) {
            warningMinutes = DEFAULT_CHAT_WARNINGS;
        }
        final List<Duration> chatWarnings = new ArrayList<>();
        for (final double minutes : warningMinutes) {
            chatWarnings.add(minutes(minutes, Duration.ZERO));
        }
        chatWarnings.sort(Comparator.reverseOrder());

        return new SessionLimitConfig(
            node.node("enabled").getBoolean(false),
            node.node("dry-run").getBoolean(true),
            node.node("server").getString("main"),
            limit,
            warning.compareTo(limit) > 0 ? limit : warning,
            minutes(node.node("break-reset-minutes").getDouble(10), Duration.ofSeconds(1)),
            minutes(node.node("queue-grace-minutes").getDouble(5), Duration.ofSeconds(1)),
            List.copyOf(chatWarnings)
        );
    }

    /**
     * Minutes may be fractional, which is handy for testing with short limits.
     */
    private static Duration minutes(final double minutes, final Duration minimum) {
        final Duration duration = Duration.ofMillis(Math.round(minutes * 60_000));
        return duration.compareTo(minimum) < 0 ? minimum : duration;
    }
}
