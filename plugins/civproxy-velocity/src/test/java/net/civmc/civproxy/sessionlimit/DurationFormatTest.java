package net.civmc.civproxy.sessionlimit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class DurationFormatTest {

    @Test
    void clockRoundsUp() {
        assertEquals("1:00:00", DurationFormat.clock(Duration.ofHours(1)));
        assertEquals("42:17", DurationFormat.clock(Duration.ofMinutes(42).plusSeconds(16).plusMillis(100)));
        assertEquals("0:01", DurationFormat.clock(Duration.ofMillis(1)));
        assertEquals("0:00", DurationFormat.clock(Duration.ofSeconds(-5)));
    }

    @Test
    void words() {
        assertEquals("8 hours", DurationFormat.words(Duration.ofHours(8)));
        assertEquals("1 hour", DurationFormat.words(Duration.ofMinutes(59).plusSeconds(59).plusMillis(500)));
        assertEquals("2 hours 5 minutes", DurationFormat.words(Duration.ofMinutes(125)));
        assertEquals("10 minutes", DurationFormat.words(Duration.ofMinutes(10)));
        assertEquals("1 minute", DurationFormat.words(Duration.ofSeconds(60)));
        assertEquals("30 seconds", DurationFormat.words(Duration.ofSeconds(30)));
    }

    @Test
    void shortPlayedRoundsDown() {
        assertEquals("5h 12m", DurationFormat.shortPlayed(Duration.ofMinutes(312).plusSeconds(59)));
        assertEquals("45m", DurationFormat.shortPlayed(Duration.ofMinutes(45)));
    }
}
