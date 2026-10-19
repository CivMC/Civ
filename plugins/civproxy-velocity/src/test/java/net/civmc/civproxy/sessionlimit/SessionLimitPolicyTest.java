package net.civmc.civproxy.sessionlimit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.Instant;
import net.civmc.civproxy.sessionlimit.SessionLimitPolicy.Action;
import net.civmc.civproxy.sessionlimit.SessionLimitPolicy.Decision;
import org.junit.jupiter.api.Test;

class SessionLimitPolicyTest {

    private static final Duration LIMIT = Duration.ofHours(8);
    private static final Duration WARNING = Duration.ofHours(1);
    private static final Duration BREAK_RESET = Duration.ofMinutes(10);
    private static final Instant NOW = Instant.parse("2026-09-18T20:00:00Z");

    private final SessionLimitPolicy policy = new SessionLimitPolicy(LIMIT, WARNING);

    @Test
    void nothingBeforeTheWarningWindow() {
        assertEquals(Action.NONE, this.policy.evaluate(Duration.ofHours(6), null, NOW, true, false).action());
    }

    @Test
    void countdownInsideTheWarningWindow() {
        final Decision decision = this.policy.evaluate(Duration.ofHours(7).plusMinutes(18), null, NOW, false, false);
        assertEquals(Action.COUNTDOWN, decision.action());
        assertEquals(Duration.ofMinutes(42), decision.remaining());
    }

    @Test
    void countdownEndingWithQueueMovesRightAway() {
        assertEquals(Action.MOVE, this.policy.evaluate(LIMIT, null, NOW, true, true).action());
    }

    @Test
    void countdownEndingWithoutQueueLetsThemPlay() {
        assertEquals(Action.OVER_LIMIT, this.policy.evaluate(LIMIT, null, NOW, false, true).action());
    }

    @Test
    void queueFormingAfterTheLimitStartsGrace() {
        assertEquals(Action.START_GRACE,
            this.policy.evaluate(Duration.ofHours(9), null, NOW, true, false).action());
    }

    @Test
    void graceCountsDownAndKeepsGoingIfTheQueueEmpties() {
        final Decision decision = this.policy.evaluate(Duration.ofHours(9), NOW.plusSeconds(200), NOW, false, false);
        assertEquals(Action.GRACE, decision.action());
        assertEquals(Duration.ofSeconds(200), decision.remaining());
    }

    @Test
    void graceEndingWithQueueMoves() {
        assertEquals(Action.MOVE, this.policy.evaluate(Duration.ofHours(9), NOW, NOW, true, false).action());
    }

    @Test
    void graceEndingWithoutQueueLapses() {
        assertEquals(Action.GRACE_LAPSED, this.policy.evaluate(Duration.ofHours(9), NOW, NOW, false, false).action());
    }

    @Test
    void relogForPriorityRightBeforeTheLimitStillGetsMoved() {
        final Instant start = NOW.minus(Duration.ofHours(8));
        final PlayStreak streak = new PlayStreak();
        streak.enter(start, BREAK_RESET, false);

        // /logout at 7h58, straight back in with leave priority a minute later
        final Instant left = start.plus(Duration.ofHours(7).plusMinutes(58));
        streak.leave(left);
        final Instant back = left.plus(Duration.ofMinutes(1));
        streak.enter(back, BREAK_RESET, true);
        assertEquals(Action.COUNTDOWN, this.policy.evaluate(streak.played(back), null, back, true, false).action());

        final Instant limitReached = back.plus(Duration.ofMinutes(2));
        assertEquals(Action.MOVE,
            this.policy.evaluate(streak.played(limitReached), null, limitReached, true, true).action());
    }

    @Test
    void relogDuringGraceComesBackToAnImmediateMove() {
        final Instant graceEnds = NOW.plus(Duration.ofMinutes(5));
        final PlayStreak streak = new PlayStreak(Duration.ofHours(8).plusMinutes(10), null, graceEnds);
        streak.enter(NOW, BREAK_RESET, false);
        streak.leave(NOW.plus(Duration.ofMinutes(4)));

        final Instant back = NOW.plus(Duration.ofMinutes(7));
        streak.enter(back, BREAK_RESET, true);
        assertEquals(Action.MOVE, this.policy.evaluate(streak.played(back), streak.graceEndsAt(), back, true, false).action());
    }
}
