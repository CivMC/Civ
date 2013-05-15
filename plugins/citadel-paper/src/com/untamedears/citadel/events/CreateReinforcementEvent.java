package com.untamedears.citadel.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.untamedears.citadel.entity.IReinforcement;

public class CreateReinforcementEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private boolean cancelled_ = false;

    private final Player player_;
    private final IReinforcement rein_;
    private final Block block_;
    private final boolean state_update_;

    // player is possibly null
    public CreateReinforcementEvent(
            final IReinforcement reinforcement,
            final Block block,
            final Player player) {
        rein_ = reinforcement;
        block_ = block;
        player_ = player;
        state_update_ = false;
    }

    // player is possibly null
    public CreateReinforcementEvent(
            final IReinforcement reinforcement,
            final Block block,
            final Player player,
            final boolean update) {
        rein_ = reinforcement;
        block_ = block;
        player_ = player;
        state_update_ = update;
    }

    public HandlerList getHandlers() {
        return CreateReinforcementEvent.getHandlerList();
    }

    public boolean isCancelled() {
        return cancelled_;
    }

    public void setCancelled(final boolean cancel) {
        cancelled_ = cancel;
    }

    public Player getPlayer() {
        return player_;
    }

    public IReinforcement getReinforcement() {
        return rein_;
    }

    public Block getBlock() {
        return block_;
    }

    public boolean isStateUpdate() {
        return state_update_;
    }
}

