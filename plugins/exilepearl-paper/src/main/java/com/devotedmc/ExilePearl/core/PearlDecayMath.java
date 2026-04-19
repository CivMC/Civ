package com.devotedmc.ExilePearl.core;

final class PearlDecayMath {

    private PearlDecayMath() {}

    static int decayPerHumanInterval(int humanIntervalMin, int decayIntervalMin, int decayAmount) {
        if (decayIntervalMin <= 0) {
            return 0;
        }
        return (humanIntervalMin / decayIntervalMin) * decayAmount;
    }

    static int intervalsRemaining(int health, int decayPerHumanInterval) {
        if (decayPerHumanInterval <= 0 || health <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) health / decayPerHumanInterval);
    }
}
