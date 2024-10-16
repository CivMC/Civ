package com.devotedmc.ExilePearl.event;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.holder.PearlHolder;
import com.google.common.base.Preconditions;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that is called when a pearl is moved
 * @author Gordon
 */
public class PearlMovedEvent extends Event {

	private final ExilePearl pearl;
	final PearlHolder from;
	final PearlHolder to;

	// Handler list for spigot events
	private static final HandlerList handlers = new HandlerList();


	/**
	 * Creates a new PlayerPearledEvent instance. Called when a player is freed.
	 * @param pearl The pearl instance
	 * @param from The source holder
	 * @param to The destination holder
	 */
	public PearlMovedEvent(final ExilePearl pearl, final PearlHolder from, final PearlHolder to) {
		Preconditions.checkNotNull(pearl, "pearl");
		Preconditions.checkNotNull(from, "from");
		Preconditions.checkNotNull(to, "to");

		this.pearl = pearl;
		this.from = from;
		this.to = to;
	}

	/**
	 * Gets the exile pearl
	 * @return The exile pearl
	 */
	public ExilePearl getPearl() {
		return pearl;
	}

	/**
	 * Gets the source holder
	 * @return The source holder
	 */
	public PearlHolder getSourceHolder() {
		return from;
	}

	/**
	 * Gets the destination holder
	 * @return The source holder
	 */
	public PearlHolder getDestinationHolder() {
		return to;
	}

	@Override
	public HandlerList getHandlers() {
	    return handlers;
	}

	public static HandlerList getHandlerList() {
	    return handlers;
	}
}
