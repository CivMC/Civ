package net.civmc.civproxy.sessionlimit;

import java.time.Duration;
import java.time.Instant;
import org.jetbrains.annotations.Nullable;

/**
 * Decides what should happen to a player on the limited server, given their streak and whether anyone is queueing.
 */
public final class SessionLimitPolicy {

    public enum Action {
        /** Plenty of time left, nothing to show */
        NONE,
        /** Inside the warning window: show the countdown */
        COUNTDOWN,
        /** Past the limit, but nobody is waiting so they can keep playing */
        OVER_LIMIT,
        /** Past the limit and a queue has formed: start the grace countdown */
        START_GRACE,
        /** Grace countdown running */
        GRACE,
        /** Grace ran out but the queue emptied: clear it and let them keep playing */
        GRACE_LAPSED,
        /** Move them to the lobby and the back of the queue */
        MOVE
    }

    /**
     * @param remaining time left on the countdown or grace countdown, zero otherwise
     */
    public record Decision(Action action, Duration remaining) {

        static Decision of(final Action action) {
            return new Decision(action, Duration.ZERO);
        }
    }

    private final Duration limit;
    private final Duration warning;

    public SessionLimitPolicy(final Duration limit, final Duration warning) {
        this.limit = limit;
        this.warning = warning;
    }

    /**
     * @param countdownJustEnded true if this player was shown the countdown on the previous check, meaning they
     *                           reached the limit here after the full warning and don't get an extra grace period
     */
    public Decision evaluate(final Duration played, final @Nullable Instant graceEndsAt, final Instant now,
                             final boolean queueWaiting, final boolean countdownJustEnded) {
        final Duration remaining = this.limit.minus(played);
        if (remaining.isPositive()) {
            if (remaining.compareTo(this.warning) <= 0) {
                return new Decision(Action.COUNTDOWN, remaining);
            }
            return Decision.of(Action.NONE);
        }

        if (graceEndsAt != null) {
            // Once started, grace runs to the end even if the queue briefly empties, so the bar doesn't flicker
            if (now.isBefore(graceEndsAt)) {
                return new Decision(Action.GRACE, Duration.between(now, graceEndsAt));
            }
            return Decision.of(queueWaiting ? Action.MOVE : Action.GRACE_LAPSED);
        }

        if (!queueWaiting) {
            return Decision.of(Action.OVER_LIMIT);
        }
        return Decision.of(countdownJustEnded ? Action.MOVE : Action.START_GRACE);
    }
}
