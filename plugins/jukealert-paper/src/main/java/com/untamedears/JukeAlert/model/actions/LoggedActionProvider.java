package com.untamedears.JukeAlert.model.actions;

import java.util.UUID;

import org.bukkit.Location;

@FunctionalInterface
public interface LoggedActionProvider {
	
	public LoggedSnitchAction get(UUID player, Location location, long time, String victim);

}
