package com.untamedears.citadel.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.events.GroupChangeType;

public class GroupChangeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private boolean cancelled_ = false;

    private final GroupChangeType changeType_;
    private final Player player_;
    private final String faction_;
    private final String targetPlayer_;

    // player is possibly null
    public GroupChangeEvent(
            final GroupChangeType changeType,
            final Player player,
            final String faction,
            final String targetPlayer) {
        changeType_ = changeType;
        player_ = player;
        faction_ = faction;
        targetPlayer_ = targetPlayer;
    }

    public HandlerList getHandlers() {
        return GroupChangeEvent.getHandlerList();
    }

    public boolean isCancelled() {
        return cancelled_;
    }

    public void setCancelled(final boolean cancel) {
        cancelled_ = cancel;
    }

    public GroupChangeType getType() {
        return changeType_;
    }

    public Player getPlayer() {
        return player_;
    }

    public String getFactionName() {
        return faction_;
    }

    public String getTargetPlayerName() {
        return targetPlayer_;
    }
}
