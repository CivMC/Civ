package com.devotedmc.ExilePearl.core;

import com.devotedmc.ExilePearl.util.Clock;
import com.google.common.base.Preconditions;
import java.util.Objects;
import java.util.UUID;

/**
 * Tracks damage dealt from a player.
 *
 * @author Gordon
 */
final class DamageRecord {

    private final Clock clock;
    private final UUID damager;
    private double amount;
    private long time;

    /**
     * Creates a new DamageRecord instance
     *
     * @param clock   The clock instance
     * @param damager The damager Id
     */
    public DamageRecord(final Clock clock, final UUID damager) {
        Preconditions.checkNotNull(clock, "clock");
        Preconditions.checkNotNull(damager, "damager");

        this.clock = clock;
        this.damager = damager;
        this.amount = 0;
        this.time = clock.getCurrentTime();
    }

    /**
     * Gets the damager
     *
     * @return The damager
     */
    public final UUID getDamager() {
        return damager;
    }

    /**
     * Gets the damage amount
     *
     * @return The damage amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Sets the damage amount
     *
     * @param amount The damage amount
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * Gets the last damage time
     *
     * @return The last damage time
     */
    public long getTime() {
        return time;
    }

    /**
     * Sets the last damage time
     *
     * @param time The last damage time
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * Adds damage to the record and marks the current time
     *
     * @param amount    The damage amount
     * @param maxAmount The max amount that should be tracked
     */
    public void recordDamage(double amount, double maxAmount) {
        this.amount = Math.min(maxAmount, this.amount + amount);
        this.time = clock.getCurrentTime();
    }

    /**
     * Decays the damage by an amount
     *
     * @param decayAmount The damage amount to decay
     * @return true if the record is still valid
     */
    public boolean decayDamage(double decayAmount) {
        amount = Math.max(0, amount - decayAmount);
        return amount > 0;
    }

    @Override
    public String toString() {
        return "DamageRecord{damager: " + damager.toString() + ", amount: " + amount + ", time: " + time + "}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(damager, amount, time);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DamageRecord other = (DamageRecord) o;

        return Objects.equals(damager, other.damager) && Objects.equals(amount, other.amount) && Objects.equals(time, other.time);
    }
}
