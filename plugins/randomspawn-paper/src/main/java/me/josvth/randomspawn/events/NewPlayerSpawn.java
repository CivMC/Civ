package me.josvth.randomspawn.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is _only_ called in conjunction with the new SpawnPoint code; for now. Basically, when a point is picked, this
 * event is called to indicate that RandomSpawn intends to spawn a player at the given location. It is possible, for instance,
 * for another plugin to capture that desire, and for whatever reason decide to cancel the attempt. If all spawn points are 
 * rejected, RandomSpawn will fall back on other configured methods, ultimately falling back on Minecraft default spawning
 * if nothing else works.
 * 
 * @author ProgrammerDan programmerdan@gmail.com
 *
 */
public class NewPlayerSpawn extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	
	private boolean isCancelled = false;
	private Location l;
	
	public NewPlayerSpawn(Location location) {
		this.l = location;
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
