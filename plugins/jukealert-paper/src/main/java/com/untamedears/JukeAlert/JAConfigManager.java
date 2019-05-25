package com.untamedears.JukeAlert;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.CoreConfigManager;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.util.ConfigParsing;

public class JAConfigManager extends CoreConfigManager {

	private ManagedDatasource db;
	private List<SnitchConfiguration> snitchConfigs;

	public JAConfigManager(ACivMod plugin) {
		super(plugin);
	}
	
	public ManagedDatasource getDatabase() {
		return db;
	}
	
	public List<SnitchConfiguration> getSnitchConfigs() {
		return snitchConfigs;
	}

	@Override
	protected boolean parseInternal(ConfigurationSection config) {
		db = (ManagedDatasource) config.get("database");
		snitchConfigs = parseSnitchConfigs(config.getConfigurationSection("snitchConfigs"));
		return true;
	}

	private List<SnitchConfiguration> parseSnitchConfigs(ConfigurationSection config) {
		List<SnitchConfiguration> result = new LinkedList<>();
		if (config == null) {
			logger.warning("No snitch configuration found in config");
			return result;
		}
		for (String key : config.getKeys(false)) {
			if (!config.isConfigurationSection(key)) {
				logger.warning("Ignoring invalid entry " + key + " at " + config.getCurrentPath());
				continue;
			}
			ConfigurationSection current = config.getConfigurationSection(key);
			if (!current.isItemStack("item")) {
				logger.warning(
						"Snitch config at " + current.getCurrentPath() + " did not specify an item, it was ignored");
				continue;
			}
			ItemStack item = current.getItemStack("item");
			int range = current.getInt("range", 10);
			long softDeleteTimer = ConfigParsing.parseTime(current.getString("softDeleteTimer", "2 weeks"),
					TimeUnit.MILLISECONDS);
			long lifeTime = ConfigParsing.parseTime(current.getString("lifeTime", "2 weeks"), TimeUnit.MILLISECONDS);
			long dormantTime = ConfigParsing.parseTime(current.getString("dormantTime", "2 weeks"),
					TimeUnit.MILLISECONDS);
			boolean displayOwnerOnBreak = current.getBoolean("displayOwnerOnBreak", true);
			boolean triggerLevers = current.getBoolean("triggerLevers", true);
			SnitchConfiguration snitchConfig = new SnitchConfiguration(range, softDeleteTimer, triggerLevers, lifeTime,
					dormantTime, item, displayOwnerOnBreak);
			logger.info("Parsed snitch configuration with: item=" + item.toString() + ", range=" + range
					+ ", softDeleteTimer=" + softDeleteTimer + ", lifeTime=" + lifeTime + ", dormanTime=" + dormantTime
					+ ", displayOwnerOnBreak=" + displayOwnerOnBreak + ", triggerLevers=" + triggerLevers);
			result.add(snitchConfig);
		}
		return result;
	}

}
