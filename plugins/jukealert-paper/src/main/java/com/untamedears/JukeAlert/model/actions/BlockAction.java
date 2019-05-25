package com.untamedears.JukeAlert.model.actions;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;

public abstract class BlockAction extends PlayerAction {

	private final Location location;
	private final Material material;

	public BlockAction(long time, UUID player, Location location, Material material) {
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

}
