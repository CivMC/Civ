package com.untamedears.JukeAlert.model.field;

import java.util.Collection;

import org.bukkit.Location;

import com.untamedears.JukeAlert.model.SnitchQTEntry;

public interface FieldManager {

	boolean isInside(Location location);
	
	Collection<SnitchQTEntry> getQTEntries();
	
}
