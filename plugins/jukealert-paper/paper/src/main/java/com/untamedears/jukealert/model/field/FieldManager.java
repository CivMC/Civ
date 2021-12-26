package com.untamedears.jukealert.model.field;

import com.untamedears.jukealert.model.SnitchQTEntry;
import java.util.Collection;
import org.bukkit.Location;

public interface FieldManager {

	boolean isInside(Location location);
	
	Collection<SnitchQTEntry> getQTEntries();
	
}
