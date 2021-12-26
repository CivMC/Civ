package com.untamedears.jukealert.model.actions.abstr;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.LoggedActionPersistence;
import java.util.UUID;
import org.bukkit.Location;

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
