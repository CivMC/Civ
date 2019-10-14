package com.untamedears.jukealert.model.field;

import java.util.Collection;

import org.bukkit.Location;

import com.untamedears.jukealert.model.SnitchQTEntry;

public interface FieldManager {

	boolean isInside(Location location);
	
	Collection<SnitchQTEntry> getQTEntries();
	
}
