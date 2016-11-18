package com.devotedmc.ExilePearl.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlFreeReason;

import vg.civcraft.mc.civmodcore.util.Guard;

/**
 * Event that is called when an exiled player is being freed
 * @author Gordon
 */
public class PlayerFreedEvent extends Event implements Cancellable {
	
	private final ExilePearl pearl;
	private final PearlFreeReason reason;
	
	private boolean cancelled;
	
	// Handler list for spigot events
	private static final HandlerList handlers = new HandlerList();
	
	
	/**
	 * Creates a new PlayerPearledEvent instance. Called when a player is freed.
	 * @param pearl The pearl instance
	 * @param reason The reason for being freed
	 */
	public PlayerFreedEvent(final ExilePearl pearl, final PearlFreeReason reason) {
		Guard.ArgumentNotNull(pearl, "pearl");
		Guard.ArgumentNotNull(reason, "reason");
		
		this.pearl = pearl;
		this.reason = reason;
	}
	
	/**
	 * Gets the exile pearl
	 * @return The exile pearl
	 */
	public ExilePearl getPearl() {
		return pearl;
	}
	
	/**
	 * Gets the freeing reason
	 * @return The freeing reason
	 */
	public PearlFreeReason getReason() {
		return reason;
	}
	
	/**
	 * Gets whether the event is cancelled
	 * @return true if the event is cancelled
	 */
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	/**
	 * Sets whether the event is cancelled
	 * @param cancelled whether the event is cancelled
	 */
	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	@Override
	public HandlerList getHandlers() {
	    return handlers;
	}
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}
}
