package com.untamedears.jukealert.model.actions;

import java.util.UUID;

import org.bukkit.Location;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggableAction;

@FunctionalInterface
public interface LoggedActionProvider {
	
	public LoggableAction get(Snitch snitch, UUID player, Location location, long time, String victim);

}
