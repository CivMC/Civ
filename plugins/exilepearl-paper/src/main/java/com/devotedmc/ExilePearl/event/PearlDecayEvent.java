package com.devotedmc.ExilePearl.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.devotedmc.ExilePearl.ExilePearl;

import vg.civcraft.mc.civmodcore.util.Guard;

/**
 * Event that is called whenever a single pearl is devayed
 * <p>
 * Pearls are decayed all at once on a regular schedule and whenever that
 * happens, this event will be called for each of them
 * 
 * @author Gordon
 */
public class PearlDecayEvent extends Event implements Cancellable {

	// Handler list for spigot events
	private static final HandlerList handlers = new HandlerList();

	private boolean cancelled;
	private ExilePearl pearl;
	private int amount;

	/**
	 * Creates a new PearlDecayEvent instance.
	 * 
	 * @param action The decay action
	 */
	public PearlDecayEvent(ExilePearl pearl, int amount) {
		Guard.ArgumentNotNull(pearl, "pearl");
		this.pearl = pearl;
		this.amount = amount;
	}

	/**
	 * Gets the pearl being decayed
	 * 
	 * @return The pearl decaying
	 */
	public ExilePearl getPearl() {
		return pearl;
	}

	/**
	 * 
	 * @return Amount of damage that will be dealt to the pearl
	 */
	public int getDamageAmount() {
		return amount;
	}

	/**
	 * Sets the amount of damage to deal to the pearl
	 * 
	 * @param amount Amount of damage to deal
	 */
	public void setDamageAmount(int amount) {
		this.amount = amount;
	}

	/**
	 * Gets whether the event is cancelled
	 * 
	 * @return true if the event is cancelled
	 */
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	/**
	 * Sets whether the event is cancelled
	 * 
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
