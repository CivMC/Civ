package com.untamedears.JukeAlert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.inventory.ItemStack;

public class SnitchConfigManager {
	
	private List<SnitchConfiguration> snitchConfigs;
	
	public SnitchConfigManager(Collection<SnitchConfiguration> snitchConfigs) {
		this.snitchConfigs = new ArrayList<>(snitchConfigs);
	}
	
	/**
	 * Gets the configuration tied to the given ItemStack
	 * @param is ItemStack to get configuration for
	 * @return Configuration with the given ItemStack or null if no such config exists
	 */
	public SnitchConfiguration getConfig(ItemStack is) {
		if (is == null) {
			return null;
		}
		for(SnitchConfiguration config : snitchConfigs) {
			if (config.getItem().isSimilar(is)) {
				return config;
			}
		}
		return null;
	}

}
