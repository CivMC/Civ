package com.devotedmc.ExilePearl.event;

import com.devotedmc.ExilePearl.ExilePearl;
import com.google.common.base.Preconditions;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that is called when a player is being exiled
 * @author Gordon
 */
public class PlayerPearledEvent extends Event implements Cancellable {

	private final ExilePearl pearl;

	private boolean cancelled;

	// Handler list for spigot events
	private static final HandlerList handlers = new HandlerList();


	/**
	 * Creates a new PlayerPearledEvent instance. Called when a new player is pearled.
	 * @param pearl The pearl instance
	 */
	public PlayerPearledEvent(final ExilePearl pearl) {
		Preconditions.checkNotNull(pearl, "pearl");

		this.pearl = pearl;
	}

	/**
	 * Gets the exile pearl
	 * @return The exile pearl
	 */
	public ExilePearl getPearl() {
		return pearl;
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
