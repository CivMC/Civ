package com.untamedears.JukeAlert.model.actions;

import java.util.UUID;

import org.bukkit.Location;

public class LoggedActionPersistence {
	
	private long time;
	private UUID player;
	private Location location;
	private String victim;
	
	public LoggedActionPersistence(UUID player, Location location, long time, String victim) {
		this.player = player;
		this.location = location;
		this.time = time;
		this.victim = victim;
	}
	
	public long getTime() {
		return time;
	}
	
	public UUID getPlayer() {
		return player;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public String getVictim() {
		return victim;
	}

}
