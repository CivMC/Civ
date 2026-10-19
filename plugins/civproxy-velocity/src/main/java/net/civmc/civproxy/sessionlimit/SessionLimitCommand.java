package net.civmc.civproxy.sessionlimit;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * /sessionlimit check|reset|set: staff tools for looking at and adjusting session timers.
 */
final class SessionLimitCommand implements SimpleCommand {

    private static final List<String> SUBCOMMANDS = List.of("check", "reset", "set");

    private final SessionLimitManager manager;
    private final ProxyServer server;

    SessionLimitCommand(final SessionLimitManager manager, final ProxyServer server) {
        this.manager = manager;
        this.server = server;
    }

    @Override
    public void execute(final Invocation invocation) {
        final CommandSource source = invocation.source();
        final String[] args = invocation.arguments();
        if (args.length < 2 || !SUBCOMMANDS.contains(args[0].toLowerCase(Locale.ROOT))) {
            usage(source, invocation.alias());
            return;
        }

        final UUID playerId = this.manager.resolvePlayer(args[1]);
        if (playerId == null) {
            source.sendPlainMessage("Player not found");
            return;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "check" -> check(source, args[1], playerId);
            case "reset" -> source.sendPlainMessage(this.manager.reset(playerId)
                ? "Reset the session timer for " + args[1]
                : "No session recorded for " + args[1]);
            case "set" -> {
                if (args.length < 3) {
                    usage(source, invocation.alias());
                    return;
                }
                final double minutes;
                try {
                    minutes = Double.parseDouble(args[2]);
                } catch (final NumberFormatException exception) {
                    source.sendPlainMessage("Minutes must be a number");
                    return;
                }
                if (minutes < 0) {
                    source.sendPlainMessage("Minutes can't be negative");
                    return;
                }
                this.manager.setPlayed(playerId, Duration.ofMillis(Math.round(minutes * 60_000)));
                source.sendPlainMessage("Set " + args[1] + "'s session time to " + args[2] + " minutes");
            }
            default -> usage(source, invocation.alias());
        }
    }

    private void check(final CommandSource source, final String name, final UUID playerId) {
        final Optional<PlayStreak.Snapshot> snapshot = this.manager.snapshot(playerId);
        if (snapshot.isEmpty()) {
            source.sendPlainMessage("No session recorded for " + name);
            return;
        }
        final PlayStreak.Snapshot streak = snapshot.get();
        final SessionLimitConfig config = this.manager.config();
        final Instant now = Instant.now();

        final StringBuilder message = new StringBuilder(name)
            .append(": ").append(DurationFormat.shortPlayed(streak.played()))
            .append(" on ").append(config.server()).append(" this session");
        if (streak.onServer()) {
            message.append(", online there now");
        } else if (streak.leftAt() != null) {
            message.append(", left ").append(DurationFormat.shortPlayed(Duration.between(streak.leftAt(), now)))
                .append(" ago");
        }
        final Duration remaining = config.limit().minus(streak.played());
        message.append(remaining.isPositive()
            ? ", " + DurationFormat.shortPlayed(remaining) + " until the limit"
            : ", past the limit");
        if (streak.graceEndsAt() != null) {
            message.append(now.isBefore(streak.graceEndsAt())
                ? ", moving in " + DurationFormat.clock(Duration.between(now, streak.graceEndsAt()))
                : ", grace used up");
        }
        message.append(". Queue waiting: ").append(this.manager.isQueueWaiting() ? "yes" : "no")
            .append(config.dryRun() ? " (dry-run mode)" : "");
        source.sendPlainMessage(message.toString());
    }

    private static void usage(final CommandSource source, final String alias) {
        source.sendPlainMessage("Usage: /" + alias + " check <player> | reset <player> | set <player> <minutes played>");
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission(SessionLimitManager.ADMIN_PERMISSION);
    }

    @Override
    public List<String> suggest(final Invocation invocation) {
        final String[] args = invocation.arguments();
        final List<String> suggestions = new ArrayList<>();
        if (args.length <= 1) {
            final String prefix = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
            SUBCOMMANDS.stream().filter(sub -> sub.startsWith(prefix)).forEach(suggestions::add);
        } else if (args.length == 2) {
            final String prefix = args[1].toLowerCase(Locale.ROOT);
            for (final Player player : this.server.getAllPlayers()) {
                if (player.getUsername().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    suggestions.add(player.getUsername());
                }
            }
        }
        return suggestions;
    }
}
