package net.minelink.ctplus.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class PlayerCombatTagEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;

    private final Player victim;

    private final Player attacker;

    private int tagDuration;

    public PlayerCombatTagEvent(Player victim, Player attacker, int tagDuration) {
        super(victim != null ? victim : attacker);
        this.victim = victim;
        this.attacker = attacker;
        this.tagDuration = tagDuration;
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

    public Player getVictim() {
        return victim;
    }

    public Player getAttacker() {
        return attacker;
    }

    public int getTagDuration() {
        return tagDuration;
    }

    public void setTagDuration(int tagDuration) {
        this.tagDuration = tagDuration;
    }

}
