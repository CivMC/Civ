package com.untamedears.JukeAlert.model.actions.abstr;

import java.util.UUID;

import org.bukkit.Location;

import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.actions.LoggedActionPersistence;

public abstract class LoggablePlayerVictimAction extends LoggablePlayerAction {
	
	protected final String victim;
	protected final Location location;

	public LoggablePlayerVictimAction(long time, Snitch snitch, UUID player, Location location, String victim) {
		super(time, snitch, player);
		this.victim = victim;
		this.location = location;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public String getVictim() {
		return victim;
	}
 	
	@Override
	public LoggedActionPersistence getPersistence() {
		return new LoggedActionPersistence(player, null, time, victim);
	}

}
