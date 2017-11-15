package com.devotedmc.ExilePearl.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.devotedmc.ExilePearl.ExilePearl;

import vg.civcraft.mc.civmodcore.util.Guard;

public class PearlSummonEvent extends Event implements Cancellable {

	private final ExilePearl pearl;
	private final Player summoner;
	
	private boolean cancelled;
	
	private static final HandlerList handlers = new HandlerList();
	
	/**
	 * Creates a new PearlSummonEvent instance. Called when a player is summoned
	 * @param pearl The pearl instance
	 * @param summoner The one summoning the pearl
	 */
	public PearlSummonEvent(final ExilePearl pearl, final Player summoner) {
		Guard.ArgumentNotNull(pearl, "pearl");
		Guard.ArgumentNotNull(summoner, "summoner");
		
		this.pearl = pearl;
		this.summoner = summoner;
	}
	
	/**
	 * Gets the exile pearl
	 * @return The exile pearl
	 */
	public ExilePearl getPearl() {
		return pearl;
	}
	
	/**
	 * Gets the summoner
	 * @return The summoner
	 */
	public Player getSummoner() {
		return summoner;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
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
