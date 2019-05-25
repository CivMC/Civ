package com.untamedears.JukeAlert.manager;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;

import com.untamedears.JukeAlert.model.Snitch;

import vg.civcraft.mc.civmodcore.locations.SparseQuadTree;

/**
 * Holds all snitches for a single world
 *
 */
public class SnitchWorldManager {
	
	private World world;
	private SparseQuadTree<SnitchQTEntry> quadTree;
	private Map<Location, Snitch> snitchesByCoordinate;
	
	public SnitchWorldManager(World world) {
		if (world == null) {
			throw new IllegalArgumentException("World may not be null");
		}
		this.world = world;
		this.quadTree = new SparseQuadTree<>();
	}
	
	/**
	 * @return World this instance is managing snitches for
	 */
	public World getWorld() {
		return world;
	}
	
	/**
	 * Gets all snitches which have the given location within their covering range
	 * 
	 * @param location Location to get covering snitches for
	 */
	public Collection<Snitch> getSnitchesCovering(Location location) {
		List<Snitch> result = new LinkedList<>();
		for(SnitchQTEntry entry : quadTree.find(location.getBlockX(), location.getBlockZ())) {
			if (entry.getSnitch().getFieldManager().isInside(location)) {
				result.add(entry.getSnitch());
			}
 		}
		return result;
	}
	
	/**
	 * Gets the snitch exactly at the given location or null if no snitch exists
	 * there
	 * 
	 * @param location Location to get snitch for
	 * @return Snitch at the location or null
	 */
	public Snitch getSnitchAt(Location location) {
		return snitchesByCoordinate.get(location);
	}
	
	/**
	 * Adds a new snitch to the tracking
	 * 
	 * @param snitch Snitch to add
	 */
	public void addSnitch(Snitch snitch) {
		for(SnitchQTEntry entry : snitch.getFieldManager().getQTEntries()) {
			quadTree.add(entry);
		}
		snitchesByCoordinate.put(snitch.getLocation(), snitch);
	}

	/**
	 * Removes a snitch from the tracking
	 * 
	 * @param snitch Snitch to remove
	 */
	public void removeSnitch(Snitch snitch) {
		for(SnitchQTEntry entry : snitch.getFieldManager().getQTEntries()) {
			quadTree.remove(entry);
		}
		snitchesByCoordinate.remove(snitch.getLocation());
	}

}
