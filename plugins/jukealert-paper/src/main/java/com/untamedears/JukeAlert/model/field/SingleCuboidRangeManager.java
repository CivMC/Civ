package com.untamedears.JukeAlert.model.field;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;

import com.untamedears.JukeAlert.manager.SnitchQTEntry;
import com.untamedears.JukeAlert.model.Snitch;

public class SingleCuboidRangeManager implements FieldManager {

	private Location center;
	private int range;
	private Snitch snitch;
	private List<SnitchQTEntry> qtEntries;

	public SingleCuboidRangeManager(Location center, int range, Snitch snitch) {
		this.center = center;
		this.range = range;
	}

	@Override
	public boolean isInside(Location location) {
		int x = center.getBlockX();
		int y = center.getBlockY();
		int z = center.getBlockZ();
		if (location.getBlockY() > (y + range) || location.getBlockY() < (y - range)) {
			return false;
		}
		if (location.getBlockX() > (x + range) || location.getBlockX() < (x - range)) {
			return false;
		}
		if (location.getBlockZ() > (z + range) || location.getBlockZ() < (z - range)) {
			return false;
		}
		return true;
	}

	@Override
	public Collection<SnitchQTEntry> getQTEntries() {
		if (qtEntries == null) {
			qtEntries = new LinkedList<>();
			qtEntries.add(new SnitchQTEntry(snitch, center.getBlockZ() - range, center.getBlockZ() + range,
					center.getBlockX() - range, center.getBlockX() + range));
		}
		return qtEntries;
	}

}
