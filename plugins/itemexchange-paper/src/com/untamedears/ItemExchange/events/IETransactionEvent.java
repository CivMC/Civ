package com.untamedears.ItemExchange.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class IETransactionEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	private Player player;
	private Location location;
	
	public IETransactionEvent(Player player, Location location) {
		this.player = player;
		this.location = location;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Location getExchangeLocation() {
		return location;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
