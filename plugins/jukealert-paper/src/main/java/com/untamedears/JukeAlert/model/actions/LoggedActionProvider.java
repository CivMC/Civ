package com.untamedears.JukeAlert.model.actions;

import java.util.UUID;

import org.bukkit.Location;

import com.untamedears.JukeAlert.model.Snitch;

@FunctionalInterface
public interface LoggedActionProvider {
	
	public LoggableAction get(Snitch snitch, UUID player, Location location, long time, String victim);

}
