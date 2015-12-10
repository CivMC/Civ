package com.github.igotyou.FactoryMod;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigParser {
	private FactoryModPlugin plugin;
	public ConfigParser(FactoryModPlugin plugin) {
		this.plugin = plugin;
	}
	
	public FactoryModManager parse() {
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		FileConfiguration config = plugin.getConfig();
		plugin.CITADEL_ENABLED = config.getBoolean("citadel_enabled", true);
		plugin.FACTORY_INTERACTION_MATERIAL = Material.getMaterial(config.getString(
				"factory_interaction_material", "STICK"));
		// Check if XP drops should be disabled
		plugin.DISABLE_EXPERIENCE = config.getBoolean("disable_experience",
				false);
		int updateTime = config.getInt("update_time");
		
		return null;
	}

}
