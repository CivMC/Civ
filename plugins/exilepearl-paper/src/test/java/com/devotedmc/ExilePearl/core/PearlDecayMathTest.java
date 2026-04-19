package com.devotedmc.ExilePearl.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PearlDecayMathTest {

    @Test
    void decayPerHumanInterval_typicalDayConfig() {
        // 1440 min/day, decay every 60 min, 1 health per tick -> 24 health/day
        Assertions.assertEquals(24, PearlDecayMath.decayPerHumanInterval(1440, 60, 1));
    }

    @Test
    void decayPerHumanInterval_zeroDecayInterval_returnsZero() {
        Assertions.assertEquals(0, PearlDecayMath.decayPerHumanInterval(1440, 0, 1));
    }

    @Test
    void decayPerHumanInterval_zeroDecayAmount_returnsZero() {
        Assertions.assertEquals(0, PearlDecayMath.decayPerHumanInterval(1440, 60, 0));
    }

    @Test
    void intervalsRemaining_exactlyDivisible() {
        Assertions.assertEquals(10, PearlDecayMath.intervalsRemaining(240, 24));
    }

    @Test
    void intervalsRemaining_roundsUp() {
        Assertions.assertEquals(11, PearlDecayMath.intervalsRemaining(241, 24));
    }

    @Test
    void intervalsRemaining_partialIntervalRoundsUpToOne() {
        Assertions.assertEquals(1, PearlDecayMath.intervalsRemaining(1, 24));
    }

    @Test
    void intervalsRemaining_zeroHealth_returnsZero() {
        Assertions.assertEquals(0, PearlDecayMath.intervalsRemaining(0, 24));
    }

    @Test
    void intervalsRemaining_negativeHealth_returnsZero() {
        Assertions.assertEquals(0, PearlDecayMath.intervalsRemaining(-5, 24));
    }

    @Test
    void intervalsRemaining_decayDisabled_returnsZero() {
        Assertions.assertEquals(0, PearlDecayMath.intervalsRemaining(100, 0));
    }
}
