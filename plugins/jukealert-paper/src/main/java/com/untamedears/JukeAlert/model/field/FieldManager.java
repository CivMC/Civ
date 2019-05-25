package com.untamedears.JukeAlert.model.field;

import java.util.Collection;

import org.bukkit.Location;

import com.untamedears.JukeAlert.manager.SnitchQTEntry;

public interface FieldManager {

	public boolean isInside(Location location);
	
	public Collection<SnitchQTEntry> getQTEntries();
	
}
