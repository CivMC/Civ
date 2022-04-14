package com.untamedears.jukealert.model.field;

import java.util.Collection;

import org.bukkit.Location;

import com.google.common.collect.Lists;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.SnitchQTEntry;

public class VariableSizeCuboidRangeManager implements FieldManager {
	
	private Snitch snitch;
	private SnitchQTEntry entry;
	private int width;
	private int height;

	public VariableSizeCuboidRangeManager(int width, int height, Snitch snitch) {
		entry = new SnitchQTEntry(snitch, snitch.getLocation(), width);
		this.snitch = snitch;
		this.width = width;
		this.height = height;
	}

	@Override
	public boolean isInside(Location location) {
		int x = snitch.getLocation().getBlockX();
		if (location.getBlockX() > (x + width) || location.getBlockX() < (x - width)) {
			return false;
		}	
		int y = snitch.getLocation().getBlockY();
		if (location.getBlockY() > (y + height) || location.getBlockY() < (y - height)) {
			return false;
		}	
		int z = snitch.getLocation().getBlockZ();
		if (location.getBlockZ() > (z + width) || location.getBlockZ() < (z - width)) {
			return false;
		}	
		return true;
	}

	@Override
	public Collection<SnitchQTEntry> getQTEntries() {
		return Lists.asList(entry, new SnitchQTEntry[0]);
	}

}
