package com.untamedears.jukealert.model.actions;

import java.util.UUID;
import org.bukkit.Location;

/**
 * Produced by actions to encapsulate saving them to the database
 *
 */
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

	public int getX() {
		if (location == null) {
			return 0;
		}
		return location.getBlockX();
	}

	public int getY() {
		if (location == null) {
			return 0;
		}
		return location.getBlockY();
	}

	public int getZ() {
		if (location == null) {
			return 0;
		}
		return location.getBlockZ();
	}

	public String getVictim() {
		return victim;
	}

}
