package com.devotedmc.ExilePearl.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.util.Guard;

public class PlayerPearledEvent extends Event implements Cancellable {
	
	private final ExilePearl pearl;
	private final Player imprisoner;
	
	private boolean cancelled;
	
	// Handler list for spigot events
	private static final HandlerList handlers = new HandlerList();
	
	
	/**
	 * Creates a new PlayerPearledEvent instance. Called when a new player is pearled.
	 * @param pearl The pearl instance
	 * @param imprisoner The imprisoner player
	 */
	public PlayerPearledEvent(final ExilePearl pearl, final Player imprisoner) {
		Guard.ArgumentNotNull(pearl, "pearl");
		Guard.ArgumentNotNull(imprisoner, "imprisoner");
		
		this.pearl = pearl;
		this.imprisoner = imprisoner;
	}
	
	/**
	 * Gets the exile pearl
	 * @return The exile pearl
	 */
	public ExilePearl getPearl() {
		return pearl;
	}

	/**
	 * Gets the imprisoner if one exists, otherwise null
	 * @return The imprisoner
	 */
	public Player getKilledBy() {
		return imprisoner;
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
