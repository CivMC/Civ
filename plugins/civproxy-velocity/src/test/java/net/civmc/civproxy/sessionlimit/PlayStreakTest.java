package net.civmc.civproxy.sessionlimit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class PlayStreakTest {

    private static final Duration BREAK_RESET = Duration.ofMinutes(10);
    private static final Instant START = Instant.parse("2026-09-18T12:00:00Z");

    private static Instant at(final Duration offset) {
        return START.plus(offset);
    }

    @Test
    void countsOnlyTimeOnTheServer() {
        final PlayStreak streak = new PlayStreak();
        streak.enter(START, BREAK_RESET, false);
        assertEquals(Duration.ofHours(2), streak.played(at(Duration.ofHours(2))));

        streak.leave(at(Duration.ofHours(2)));
        assertEquals(Duration.ofHours(2), streak.played(at(Duration.ofHours(2).plusMinutes(5))));
        assertFalse(streak.isOnServer());
    }

    @Test
    void shortBreakCarriesOn() {
        final PlayStreak streak = new PlayStreak();
        streak.enter(START, BREAK_RESET, false);
        final Instant left = at(Duration.ofHours(7).plusMinutes(58));
        streak.leave(left);

        assertFalse(streak.enter(left.plus(Duration.ofMinutes(1)), BREAK_RESET, false));
        assertEquals(Duration.ofHours(7).plusMinutes(59), streak.played(left.plus(Duration.ofMinutes(2))));
    }

    @Test
    void breakOfResetLengthStartsOver() {
        final PlayStreak streak = new PlayStreak();
        streak.enter(START, BREAK_RESET, false);
        final Instant left = at(Duration.ofHours(7));
        streak.leave(left);

        assertTrue(streak.enter(left.plus(BREAK_RESET), BREAK_RESET, false));
        assertEquals(Duration.ZERO, streak.played(left.plus(BREAK_RESET)));
    }

    @Test
    void returningWithLeavePriorityNeverStartsOver() {
        final PlayStreak streak = new PlayStreak();
        streak.enter(START, BREAK_RESET, false);
        final Instant left = at(Duration.ofHours(7).plusMinutes(58));
        streak.leave(left);

        // Stuck behind other priority players for a while, but still skipped the normal queue
        final Instant back = left.plus(Duration.ofMinutes(14));
        assertFalse(streak.enter(back, BREAK_RESET, true));
        assertEquals(Duration.ofHours(7).plusMinutes(58), streak.played(back));
    }

    @Test
    void resetDuringVisitRestartsFromNow() {
        final PlayStreak streak = new PlayStreak();
        streak.enter(START, BREAK_RESET, false);
        streak.startGrace(at(Duration.ofHours(8)));
        streak.reset(at(Duration.ofHours(8)));

        assertNull(streak.graceEndsAt());
        assertEquals(Duration.ofMinutes(3), streak.played(at(Duration.ofHours(8).plusMinutes(3))));
    }

    @Test
    void graceSurvivesShortBreakButNotLongOne() {
        final PlayStreak streak = new PlayStreak(Duration.ofHours(8), START, at(Duration.ofMinutes(5)));
        streak.enter(at(Duration.ofMinutes(2)), BREAK_RESET, false);
        assertEquals(at(Duration.ofMinutes(5)), streak.graceEndsAt());
        streak.leave(at(Duration.ofMinutes(3)));

        streak.enter(at(Duration.ofMinutes(3)).plus(BREAK_RESET), BREAK_RESET, false);
        assertNull(streak.graceEndsAt());
    }

    @Test
    void repeatedEnterAndLeaveAreIgnored() {
        final PlayStreak streak = new PlayStreak();
        streak.enter(START, BREAK_RESET, false);
        streak.enter(at(Duration.ofHours(1)), BREAK_RESET, false);
        streak.leave(at(Duration.ofHours(2)));
        streak.leave(at(Duration.ofHours(3)));

        assertEquals(Duration.ofHours(2), streak.played(at(Duration.ofHours(4))));
    }

    @Test
    void staleOnlyOnceOffTheServerLongEnough() {
        final PlayStreak streak = new PlayStreak();
        streak.enter(START, BREAK_RESET, false);
        assertFalse(streak.isStale(at(Duration.ofDays(1)), Duration.ofHours(6)));

        streak.leave(at(Duration.ofHours(1)));
        assertFalse(streak.isStale(at(Duration.ofHours(6)), Duration.ofHours(6)));
        assertTrue(streak.isStale(at(Duration.ofHours(7)), Duration.ofHours(6)));
    }
}
