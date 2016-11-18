package com.devotedmc.ExilePearl.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import vg.civcraft.mc.civmodcore.util.Guard;

/**
 * Event that is called when a decay operation occurs. 
 * <p>
 * This event is called twice for every decay operation. Once at the beginning
 * and once when the decay is complete. The start operation can be cancelled.
 * @author Gordon
 */
public class PearlDecayEvent extends Event implements Cancellable {
	
	public enum DecayAction { START, COMPLETE };
	
	private final DecayAction action;
	private boolean cancelled;
	
	// Handler list for spigot events
	private static final HandlerList handlers = new HandlerList();
	
	
	/**
	 * Creates a new PearlDecayEvent instance. 
	 * @param action The decay action
	 */
	public PearlDecayEvent(final DecayAction action) {
		Guard.ArgumentNotNull(action, "action");
		
		this.action = action;
	}
	
	/**
	 * Gets the decay action
	 * @return The decay action
	 */
	public DecayAction getAction() {
		return action;
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
