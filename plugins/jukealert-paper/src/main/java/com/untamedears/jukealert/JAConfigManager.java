package com.untamedears.jukealert;

import org.bukkit.configuration.ConfigurationSection;

import com.untamedears.jukealert.model.SnitchTypeManager;

import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.CoreConfigManager;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class JAConfigManager extends CoreConfigManager {

	private ManagedDatasource db;
	private SnitchTypeManager typeMan;

	public JAConfigManager(ACivMod plugin, SnitchTypeManager typeMan) {
		super(plugin);
		this.typeMan = typeMan;
	}
	
	public ManagedDatasource getDatabase() {
		return db;
	}

	@Override
	protected boolean parseInternal(ConfigurationSection config) {
		db = (ManagedDatasource) config.get("database");
		parseSnitchConfigs(config.getConfigurationSection("snitchConfigs"));
		return true;
	}

	private void parseSnitchConfigs(ConfigurationSection config) {
		if (config == null) {
			logger.warning("No snitch configuration found in config");
			return;
		}
		for (String key : config.getKeys(false)) {
			if (!config.isConfigurationSection(key)) {
				logger.warning("Ignoring invalid entry " + key + " at " + config.getCurrentPath());
				continue;
			}
			ConfigurationSection current = config.getConfigurationSection(key);
			typeMan.parseFromConfig(current);
		}
	}

}
