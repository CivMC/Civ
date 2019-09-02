package com.untamedears.JukeAlert.model;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.model.factory.LoggingSnitchFactory;
import com.untamedears.JukeAlert.model.factory.NonLoggingSnitchFactory;
import com.untamedears.JukeAlert.model.factory.SnitchConfigFactory;

public class SnitchTypeManager {

	private Map<String, Class<? extends SnitchConfigFactory>> rawSnitchConfigs;
	private Map<ItemStack, SnitchConfigFactory> configFactoriesByItem;
	private Map<Integer, SnitchConfigFactory> configFactoriesById;

	public SnitchTypeManager() {
		rawSnitchConfigs = new HashMap<>();
		configFactoriesByItem = new HashMap<>();
		configFactoriesById = new HashMap<>();
		registerSnitchTypes();
	}
	
	private void registerSnitchTypes() {
		rawSnitchConfigs.put("NON-LOGGING", NonLoggingSnitchFactory.class);
		rawSnitchConfigs.put("LOGGING", LoggingSnitchFactory.class);
	}

	public void parseFromConfig(ConfigurationSection config) {
		Logger logger = JukeAlert.getInstance().getLogger();
		String type = config.getString("type").toUpperCase();
		if (type == null) {
			logger.warning(
					"Snitch config specified at " + config.getCurrentPath() + " did not have a type, it was ignored");
			return;
		}
		Class<? extends SnitchConfigFactory> clazz = rawSnitchConfigs.get(type);
		if (clazz == null) {
			logger.warning("Snitch config specified at " + config.getCurrentPath() + " had unknown type " + type
					+ ", it was ignored");
			return;
		}
		SnitchConfigFactory configFactory;
		try {
			configFactory = clazz.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			logger.log(Level.SEVERE, "Failed setup snitch config factory", e);
			return;
		}
		if (!configFactory.parse(config, logger)) {
			logger.warning("Ignoring snitch config at " + config.getCurrentPath());
			return;
		}
		configFactoriesById.put(configFactory.getID(), configFactory);
		configFactoriesByItem.put(configFactory.getItem(), configFactory);
	}

	/**
	 * Gets the configuration tied to the given ItemStack
	 * 
	 * @param is ItemStack to get configuration for
	 * @return Configuration with the given ItemStack or null if no such config
	 *         exists
	 */
	public SnitchConfigFactory getConfig(ItemStack is) {
		if (is == null) {
			return null;
		}
		ItemStack copy = is.clone();
		copy.setAmount(1);
		return configFactoriesByItem.get(copy);
	}
	
	public SnitchConfigFactory getConfig(int id) {
		return configFactoriesById.get(id);
	}

}
