package com.untamedears.jukealert;

import com.untamedears.jukealert.model.SnitchTypeManager;
import org.bukkit.configuration.ConfigurationSection;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.config.ConfigParser;
import vg.civcraft.mc.civmodcore.dao.DatabaseCredentials;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class JAConfigManager extends ConfigParser {
	private SnitchTypeManager typeMan;

	public JAConfigManager(ACivMod plugin, SnitchTypeManager typeMan) {
		super(plugin);
		this.typeMan = typeMan;
	}
	
	public ManagedDatasource getDatabase(ConfigurationSection config) {
		return ManagedDatasource.construct((ACivMod) plugin, (DatabaseCredentials) config.get("database"));
	}

	@Override
	protected boolean parseInternal(ConfigurationSection config) {
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
