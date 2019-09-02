package com.untamedears.JukeAlert.model.factory;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.untamedears.JukeAlert.model.Snitch;

import vg.civcraft.mc.namelayer.group.Group;

public abstract class SnitchConfigFactory {
	
	protected int id;
	protected ItemStack item;
	
	public boolean parse(ConfigurationSection config, Logger logger) {
		item = config.getItemStack("item", null);
		if (item == null) {
			logger.warning("Snitch type at " + config.getCurrentPath() + " had no item specified");
			return false;
		}
		if (!config.isInt("id")) {
			logger.warning("Snitch type at " + config.getCurrentPath() + " had no id specified");
			return false;
		}
		id = config.getInt("id");
		return true;
	}
	
	public abstract Snitch create(Location location, Group group, Player player);
	
	public abstract Snitch recreate(Location location, String name, int groupID, long lastRefresh, boolean canTriggerLevers);
	
	/**
	 * @return Identifying id of this config which will identify its instances even across config changes
	 */
	public int getID() {
		return id;
	}
	
	/**
	 * @return Item used to create instances of this snitch
	 */
	public ItemStack getItem() {
		return item;
	}
}
