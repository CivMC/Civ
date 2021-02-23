package com.untamedears.jukealert.model.field;

import com.google.common.collect.Lists;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.SnitchQTEntry;
import java.util.Collection;
import org.bukkit.Location;

public class SingleCuboidRangeManager implements FieldManager {

	private Snitch snitch;
	private SnitchQTEntry entry;
	private int range;

	public SingleCuboidRangeManager(int range, Snitch snitch) {
		entry = new SnitchQTEntry(snitch, snitch.getLocation(), range);
		this.snitch = snitch;
		this.range = range;
	}

	@Override
	public boolean isInside(Location location) {
		int x = snitch.getLocation().getBlockX();
		if (location.getBlockX() > (x + range) || location.getBlockX() < (x - range)) {
			return false;
		}	
		int y = snitch.getLocation().getBlockY();
		if (location.getBlockY() > (y + range) || location.getBlockY() < (y - range)) {
			return false;
		}	
		int z = snitch.getLocation().getBlockZ();
		if (location.getBlockZ() > (z + range) || location.getBlockZ() < (z - range)) {
			return false;
		}	
		return true;
	}

	@Override
	public Collection<SnitchQTEntry> getQTEntries() {
		return Lists.asList(entry, new SnitchQTEntry[0]);
	}

}
