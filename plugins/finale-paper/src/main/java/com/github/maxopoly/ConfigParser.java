package com.github.maxopoly;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigParser {
	private Finale plugin;
	private FinaleManager manager;
	
	public ConfigParser(Finale plugin) {
		this.plugin = plugin;
	}
	
	public FinaleManager parse() {
		plugin.info("Parsing config");
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		FileConfiguration config = plugin.getConfig();
		double attackSpeed = config.getDouble("attackSpeed", 4);
		manager = new FinaleManager(attackSpeed);
		return manager;
	}

}
