package net.civmc.civproxy.sessionlimit;

import java.time.Duration;
import java.time.Instant;
import org.jetbrains.annotations.Nullable;

/**
 * Time a player has spent on the limited server since their last real break.
 * <p>
 * Time off the server pauses the streak. It only resets after a break of at least the configured length, and a
 * player who comes back through the queue using the priority they got for leaving keeps their streak no matter how
 * long they were gone, so relogging for leave priority can't be used to dodge the limit.
 */
public final class PlayStreak {

    private Duration banked;
    private @Nullable Instant visitStart;
    private @Nullable Instant leftAt;
    private @Nullable Instant graceEndsAt;

    public PlayStreak() {
        this(Duration.ZERO, null, null);
    }

    public PlayStreak(final Duration played, final @Nullable Instant leftAt, final @Nullable Instant graceEndsAt) {
        this.banked = played;
        this.leftAt = leftAt;
        this.graceEndsAt = graceEndsAt;
    }

    public record Snapshot(Duration played, boolean onServer, @Nullable Instant leftAt, @Nullable Instant graceEndsAt) {

    }

    /**
     * @return true if the break was long enough that the streak started over
     */
    public synchronized boolean enter(final Instant now, final Duration breakReset, final boolean returnedWithLeavePriority) {
        if (this.visitStart != null) {
            return false;
        }
        boolean reset = false;
        if (this.leftAt != null && !returnedWithLeavePriority && !now.isBefore(this.leftAt.plus(breakReset))) {
            clear();
            reset = true;
        }
        this.visitStart = now;
        this.leftAt = null;
        return reset;
    }

    public synchronized void leave(final Instant now) {
        if (this.visitStart == null) {
            return;
        }
        this.banked = this.banked.plus(positive(Duration.between(this.visitStart, now)));
        this.visitStart = null;
        this.leftAt = now;
    }

    public synchronized Duration played(final Instant now) {
        if (this.visitStart == null) {
            return this.banked;
        }
        return this.banked.plus(positive(Duration.between(this.visitStart, now)));
    }

    public synchronized boolean isOnServer() {
        return this.visitStart != null;
    }

    public synchronized void reset(final Instant now) {
        setPlayed(now, Duration.ZERO);
    }

    public synchronized void setPlayed(final Instant now, final Duration played) {
        this.banked = played;
        this.graceEndsAt = null;
        if (this.visitStart != null) {
            this.visitStart = now;
        }
    }

    public synchronized @Nullable Instant graceEndsAt() {
        return this.graceEndsAt;
    }

    public synchronized void startGrace(final Instant endsAt) {
        this.graceEndsAt = endsAt;
    }

    public synchronized void clearGrace() {
        this.graceEndsAt = null;
    }

    /**
     * @return true if the player has been off the server for at least {@code keepFor}
     */
    public synchronized boolean isStale(final Instant now, final Duration keepFor) {
        return this.visitStart == null && this.leftAt != null && !now.isBefore(this.leftAt.plus(keepFor));
    }

    public synchronized Snapshot snapshot(final Instant now) {
        return new Snapshot(played(now), this.visitStart != null, this.leftAt, this.graceEndsAt);
    }

    private void clear() {
        this.banked = Duration.ZERO;
        this.graceEndsAt = null;
    }

    private static Duration positive(final Duration duration) {
        return duration.isNegative() ? Duration.ZERO : duration;
    }
}
