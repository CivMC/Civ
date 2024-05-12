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
	private static final HandlerList _handlers = new HandlerList();
	
	private final List<Location> _impacted;
	
	public CastleGatesDrawGateEvent(final List<Location> impacted) {
		this._impacted = impacted;
	}

	public List<Location> getImpacted() {
		return this._impacted;
	}

	@Override
	public HandlerList getHandlers() {
		return CastleGatesDrawGateEvent._handlers;
	}

	public static HandlerList getHandlerList() {
		return _handlers;
	}
}
