package com.untamedears.realisticbiomes.model.ltree;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class LStep {
	
	private LStepConfig config;
	private Location location;
	private Vector direction;
	private int depth;
	
	public LStep(LStepConfig config, Location location, Vector direction, int depth) {
		this.location = location;
		this.config = config;
		this.direction = direction;
		this.depth = depth;
		
	}
	
	public List<LStep> apply() {
		return config.progress(this);
	}
	
	public Vector getDirection() {
		return direction;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public LStepConfig getConfig() {
		return config;
	}
	
	public int getDepth() {
		return depth;
	}

}
