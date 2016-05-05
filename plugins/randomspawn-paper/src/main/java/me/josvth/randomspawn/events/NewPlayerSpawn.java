package me.josvth.randomspawn.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class NewPlayerSpawn extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	
	private boolean isCancelled = false;
	private Player p;
	private Location l;
	
	public NewPlayerSpawn(Player player, Location location) {
		this.p = player;
		this.l = location;
	}
	
	public Player getPlayer() {
		return p;
	}
	
	public Location getLocation() {
		return l;
	}
	
	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean value) {
		isCancelled = value;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

}
