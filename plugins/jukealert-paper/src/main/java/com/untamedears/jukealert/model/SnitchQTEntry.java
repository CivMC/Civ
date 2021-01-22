package com.untamedears.jukealert.model;

import org.bukkit.Location;
import vg.civcraft.mc.civmodcore.locations.QTBoxImpl;

public class SnitchQTEntry extends QTBoxImpl {

	private final Snitch snitch;

	public SnitchQTEntry(Snitch snitch, Location loc, int range) {
		super(loc, range);
		this.snitch = snitch;
	}
	
	public Snitch getSnitch() {
		return snitch;
	}

}
