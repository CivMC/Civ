package com.programmerdan.minecraft.simpleadminhacks.framework.utilities;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/** Main-thread admission controller. All timestamps are monotonic nanoseconds. */
public final class PlayerCapController {

    private static final double MAX_MSPT = 500;

    public record Settings(int minCap, int maxCap, double sampleSeconds, double ewmaHalfLifeSeconds,
                           double reduceAboveMspt, double reduceHoldSeconds, int reduceStep,
                           double increaseBelowMspt, double recoverySeconds, double settleSeconds) {

        public Settings {
            if (minCap < 0 || maxCap < minCap || reduceStep < 1
                || !positive(sampleSeconds) || !positive(ewmaHalfLifeSeconds)
                || !positive(reduceHoldSeconds) || !positive(recoverySeconds) || !positive(settleSeconds)
                || !positive(reduceAboveMspt) || !positive(increaseBelowMspt)
                || increaseBelowMspt >= reduceAboveMspt) {
                throw new IllegalArgumentException("Invalid automatic player cap settings");
            }
        }

        private static boolean positive(final double value) {
            return Double.isFinite(value) && value > 0;
        }
    }

    private final Settings settings;
    private int cap;
    private long sampleStart;
    private int ticks;
    private double tickDurationTotal;
    private double sampleMspt = Double.NaN;
    private double smoothedMspt = Double.NaN;
    private double poorSeconds;
    private double healthySeconds;
    private long lastGrowth;
    private int observedPopulation;

    public PlayerCapController(@NotNull final Settings settings, final int online, final long now) {
        this.settings = Objects.requireNonNull(settings);
        this.cap = Math.clamp(online, settings.minCap(), settings.maxCap());
        this.sampleStart = now;
        this.lastGrowth = now;
        this.observedPopulation = online;
    }

    public void tick(final long now, final int online, final double tickDurationMillis) {
        if (!Double.isFinite(tickDurationMillis) || tickDurationMillis < 0) {
            throw new IllegalArgumentException("Tick duration must be finite and non-negative");
        }
        ticks++;
        tickDurationTotal += Math.min(tickDurationMillis, MAX_MSPT);
        final double elapsed = seconds(now - sampleStart);
        if (elapsed < settings.sampleSeconds()) {
            return;
        }
        sampleMspt = tickDurationTotal / ticks;
        final double previousMspt = smoothedMspt;
        final double alpha = 1 - Math.pow(2, -elapsed / settings.ewmaHalfLifeSeconds());
        smoothedMspt = Double.isNaN(smoothedMspt) ? sampleMspt : smoothedMspt + alpha * (sampleMspt - smoothedMspt);
        ticks = 0;
        tickDurationTotal = 0;
        sampleStart = now;

        // Start each confirmation period at the first qualifying sample, not before it.
        poorSeconds = smoothedMspt > settings.reduceAboveMspt()
            ? (previousMspt > settings.reduceAboveMspt() ? poorSeconds + elapsed : 0) : 0;
        healthySeconds = smoothedMspt < settings.increaseBelowMspt()
            ? (previousMspt < settings.increaseBelowMspt() ? healthySeconds + elapsed : 0) : 0;

        if (poorSeconds >= settings.reduceHoldSeconds()) {
            cap = Math.max(settings.minCap(), Math.min(cap, online) - settings.reduceStep());
            poorSeconds = 0;
        } else if (isReady(now) && online == cap && cap < settings.maxCap()) {
            // Offer only one slot, and never accumulate unused offers.
            // After a reduction, observe growth from the new, lower population.
            observedPopulation = online;
            cap++;
        }
        if (!isReady(now)) {
            // Withdraw an unused growth offer, but retain established slots for replacements.
            cap = Math.max(settings.minCap(), Math.min(cap, observedPopulation));
        }
    }

    public void joined(final long now, final int online) {
        if (online > observedPopulation) {
            observedPopulation = online;
            lastGrowth = now;
        }
    }

    private boolean isReady(final long now) {
        return smoothedMspt < settings.increaseBelowMspt()
            && healthySeconds >= settings.recoverySeconds()
            && getSettlingRemaining(now) == 0;
    }

    public int getCap() {
        return cap;
    }

    public double getSampleMspt() {
        return sampleMspt;
    }

    public double getSmoothedMspt() {
        return smoothedMspt;
    }

    public double getHealthySeconds() {
        return healthySeconds;
    }

    public double getSettlingRemaining(final long now) {
        return Math.max(0, settings.settleSeconds() - seconds(now - lastGrowth));
    }

    private static double seconds(final long nanos) {
        return nanos / 1_000_000_000.0;
    }
}
