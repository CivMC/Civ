package com.untamedears.JukeAlert.model.actions;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;

public abstract class LoggableBlockAction extends LoggablePlayerAction {

	private final Location location;
	private final Material material;

	public LoggableBlockAction(long time, UUID player, Location location, Material material) {
		super(time, player);
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
