package net.minelink.ctplus.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import static com.google.common.base.Preconditions.*;

public final class CombatLogEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Reason reason;

    private boolean cancelled;

    public CombatLogEvent(Player player, Reason reason) {
        super(checkNotNull(player, "Null player"));
        this.reason = checkNotNull(reason, "Null reason");
    }

    public Reason getReason() {
        return reason;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public enum Reason {
        TAGGED,
        UNSAFE_LOGOUT;
    }
}
