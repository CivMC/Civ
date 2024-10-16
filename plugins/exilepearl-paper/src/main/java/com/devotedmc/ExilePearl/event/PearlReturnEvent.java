package com.devotedmc.ExilePearl.event;

import com.devotedmc.ExilePearl.ExilePearl;
import com.google.common.base.Preconditions;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PearlReturnEvent extends Event implements Cancellable {

	private final ExilePearl pearl;

	private boolean cancelled;

	private static final HandlerList handlers = new HandlerList();

	public PearlReturnEvent(final ExilePearl pearl) {
		Preconditions.checkNotNull(pearl, "pearl");

		this.pearl = pearl;
	}

	/**
	 * Gets the pearl for being returned
	 * @return the pearl
	 */
	public ExilePearl getPearl() {
		return pearl;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
