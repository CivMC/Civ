package net.civmc.civproxy.sessionlimit;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * /sessiontime: lets players check how long they have left.
 */
final class SessionTimeCommand implements SimpleCommand {

    private final SessionLimitManager manager;

    SessionTimeCommand(final SessionLimitManager manager) {
        this.manager = manager;
    }

    @Override
    public void execute(final Invocation invocation) {
        if (!(invocation.source() instanceof final Player player)) {
            invocation.source().sendPlainMessage("Only players have a session time");
            return;
        }
        player.sendMessage(Component.text(describe(player), NamedTextColor.YELLOW));
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        // Don't tell players about a limit that isn't being enforced yet
        return !this.manager.config().dryRun()
            || invocation.source().hasPermission(SessionLimitManager.ADMIN_PERMISSION);
    }

    private String describe(final Player player) {
        final SessionLimitConfig config = this.manager.config();
        if (player.hasPermission(SessionLimitManager.BYPASS_PERMISSION)) {
            return "You're exempt from the session limit.";
        }

        final String server = config.server();
        final String limit = DurationFormat.words(config.limit());
        final Instant now = Instant.now();
        final Optional<PlayStreak.Snapshot> snapshot = this.manager.snapshot(player.getUniqueId());
        if (snapshot.isEmpty() || snapshot.get().played().isZero()) {
            return "Your session timer starts when you join " + server + ".";
        }

        final PlayStreak.Snapshot streak = snapshot.get();
        final String played = DurationFormat.shortPlayed(streak.played());
        if (!streak.onServer()) {
            if (streak.leftAt() != null && !now.isBefore(streak.leftAt().plus(config.breakReset()))) {
                return "Your session timer will start fresh the next time you join " + server + ".";
            }
            return "You've played " + played + " on " + server + " without a break.";
        }

        final Duration remaining = config.limit().minus(streak.played());
        if (remaining.isPositive()) {
            return "You've played " + played + " on " + server + " without a break. You reach the " + limit
                + " limit in " + DurationFormat.words(remaining) + ".";
        }
        if (streak.graceEndsAt() != null && now.isBefore(streak.graceEndsAt())) {
            return "You're past the " + limit + " limit and players are queueing. You'll be moved to the lobby in "
                + DurationFormat.clock(Duration.between(now, streak.graceEndsAt())) + ". Type /logout to leave safely.";
        }
        return "You're past the " + limit + " limit. You can keep playing while nobody is waiting in the queue; if"
            + " a queue forms, you'll get " + DurationFormat.words(config.queueGrace()) + " of warning.";
    }
}
