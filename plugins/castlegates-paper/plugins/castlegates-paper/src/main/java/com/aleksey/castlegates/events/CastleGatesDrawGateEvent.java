package com.aleksey.castlegates.events;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called after the Gear is fully drawn (withdrawn). Lets other plugins update themselves based on the blocks changed.
 * 
 * @author ProgrammerDan
 *
 */
public class CastleGatesDrawGateEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	private List<Location> impacted; 
	
	public CastleGatesDrawGateEvent(final List<Location> impacted) {
		this.impacted = impacted;
	}
	
	public List<Location> getImpacted() {
		return this.impacted;
	}
	
	@Override
	public HandlerList getHandlers() {
		return CastleGatesDrawGateEvent.handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
