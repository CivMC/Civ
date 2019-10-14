package com.untamedears.jukealert.model.actions.abstr;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.LoggedActionPersistence;

public abstract class LoggableBlockAction extends LoggablePlayerAction {

	protected final Location location;
	protected final Material material;

	public LoggableBlockAction(long time, Snitch snitch, UUID player, Location location, Material material) {
		super(time, snitch, player);
		this.location = location;
		this.material = material;
	}

	/**
	 * @return Where the block was for which the action occured
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * @return Material of the block this action is about
	 */
	public Material getMaterial() {
		return material;
	}
	
	@Override
	public LoggedActionPersistence getPersistence() {
		return new LoggedActionPersistence(getPlayer(), location, time, material.name());
	}

}
