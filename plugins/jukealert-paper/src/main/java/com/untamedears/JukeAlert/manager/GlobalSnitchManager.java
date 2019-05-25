package com.untamedears.JukeAlert.manager;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Location;

import com.untamedears.JukeAlert.model.Snitch;

public class GlobalSnitchManager {

	private Map<UUID, SnitchWorldManager> worldManagers;

	public GlobalSnitchManager() {
		this.worldManagers = new TreeMap<>();
	}

	/**
	 * Gets all snitches which have the given location within their covering range
	 * 
	 * @param location Location to get covering snitches for
	 */
	public Collection<Snitch> getSnitchesCovering(Location location) {
		return getWorldManager(location).getSnitchesCovering(location);
	}

	/**
	 * Gets the snitch exactly at the given location or null if no snitch exists
	 * there
	 * 
	 * @param location Location to get snitch for
	 * @return Snitch at the location or null
	 */
	public Snitch getSnitchAt(Location location) {
		return getWorldManager(location).getSnitchAt(location);
	}

	/**
	 * Adds a new snitch to the tracking
	 * 
	 * @param snitch Snitch to add
	 */
	public void addSnitch(Snitch snitch) {
		getWorldManager(snitch.getLocation()).addSnitch(snitch);
	}

	/**
	 * Removes a snitch from the tracking
	 * 
	 * @param snitch Snitch to remove
	 */
	public void removeSnitch(Snitch snitch) {
		getWorldManager(snitch.getLocation()).removeSnitch(snitch);
	}

	private SnitchWorldManager getWorldManager(Location location) {
		return worldManagers.get(location.getWorld().getUID());
	}

}
