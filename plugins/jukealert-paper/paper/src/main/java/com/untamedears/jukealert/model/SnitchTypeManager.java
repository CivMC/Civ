package com.untamedears.jukealert.model;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.model.appender.AbstractSnitchAppender;
import com.untamedears.jukealert.model.appender.BroadcastEntryAppender;
import com.untamedears.jukealert.model.appender.DormantCullingAppender;
import com.untamedears.jukealert.model.appender.LeverToggleAppender;
import com.untamedears.jukealert.model.appender.ShowOwnerOnDestroyAppender;
import com.untamedears.jukealert.model.appender.SnitchLogAppender;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class SnitchTypeManager {

	private Map<String, Class<? extends AbstractSnitchAppender>> appenderClasses;
	private Map<ItemStack, SnitchFactoryType> configFactoriesByItem;
	private Map<Integer, SnitchFactoryType> configFactoriesById;

	public SnitchTypeManager() {
		appenderClasses = new HashMap<>();
		configFactoriesByItem = new HashMap<>();
		configFactoriesById = new HashMap<>();
		registerAppenderTypes();
	}

	private void registerAppenderTypes() {
		registerAppenderType(BroadcastEntryAppender.ID, BroadcastEntryAppender.class);
		registerAppenderType(SnitchLogAppender.ID, SnitchLogAppender.class);
		registerAppenderType(LeverToggleAppender.ID, LeverToggleAppender.class);
		registerAppenderType(DormantCullingAppender.ID, DormantCullingAppender.class);
		registerAppenderType(ShowOwnerOnDestroyAppender.ID, ShowOwnerOnDestroyAppender.class);
	}

	private void registerAppenderType(String id, Class<? extends AbstractSnitchAppender> clazz) {
		appenderClasses.put(id.toLowerCase(), clazz);
	}

	public boolean parseFromConfig(ConfigurationSection config) {
		Logger logger = JukeAlert.getInstance().getLogger();
		ItemStack item = config.getItemStack("item", null);
		StringBuilder sb = new StringBuilder();
		if (item == null) {
			logger.warning("Snitch type at " + config.getCurrentPath() + " had no item specified");
			return false;
		}
		if (!config.isInt("id")) {
			logger.warning("Snitch type at " + config.getCurrentPath() + " had no id specified");
			return false;
		}
		int id = config.getInt("id");
		if (!config.isString("name")) {
			logger.warning("Snitch type at " + config.getCurrentPath() + " had no name specified");
			return false;
		}
		String name = config.getString("name");
		if (!config.isInt("range")) {
			logger.warning("Snitch type at " + config.getCurrentPath() + " had no range specified");
			return false;
		}
		sb.append("Successfully parsed type ");
		sb.append(name);
		sb.append(" with id: ");
		sb.append(id);
		sb.append(", item: ");
		sb.append(item.toString());
		int range = config.getInt("range");
		sb.append(", range: ");
		sb.append(range);
		sb.append(", appenders: ");
		List<Function<Snitch, AbstractSnitchAppender>> appenderInstanciations = new ArrayList<>();
		if (config.isConfigurationSection("appender")) {
			ConfigurationSection appenderSection = config.getConfigurationSection("appender");
			for (String key : appenderSection.getKeys(false)) {
				if (!appenderSection.isConfigurationSection(key)) {
					logger.warning("Ignoring invalid entry " + key + " at " + appenderSection);
					continue;
				}
				Class<? extends AbstractSnitchAppender> appenderClass = appenderClasses.get(key.toLowerCase());
				if (appenderClass == null) {
					logger.warning("Appender " + key + " at " + appenderSection + " is of an unknown type");
					// this is not something we should just ignore, disregard entire config in this
					// case
					return false;
				}
				ConfigurationSection entrySection = appenderSection.getConfigurationSection(key);
				Function<Snitch, AbstractSnitchAppender> instanciation = getAppenderInstantiation(
						appenderClass, entrySection);
				appenderInstanciations.add(instanciation);
				sb.append(appenderClass.getSimpleName());
				sb.append("   ");
			}
		}
		if (appenderInstanciations.isEmpty()) {
			logger.warning("Snitch config at "  + config.getCurrentPath() + " has no appenders, this is likely not what you intended");
		}
		SnitchFactoryType configFactory = new SnitchFactoryType(item, range, name, id, appenderInstanciations);
		configFactoriesById.put(configFactory.getID(), configFactory);
		configFactoriesByItem.put(configFactory.getItem(), configFactory);
		logger.info(sb.toString());
		return true;
	}

	/**
	 * Creates a function which will instanciate the appender based on the
	 * ConfigurationSection give to it
	 * 
	 * @param clazz Class of the appender
	 * @return Function to instanciate appenders of the given class or null if the
	 *         appender has no appropriate constructor
	 */
	private Function<Snitch, AbstractSnitchAppender> getAppenderInstantiation(
			Class<? extends AbstractSnitchAppender> clazz, ConfigurationSection config) {
		try {
			Constructor<? extends AbstractSnitchAppender> constructor = clazz
					.getConstructor(Snitch.class, ConfigurationSection.class);
			return s -> {
				try {
					return constructor.newInstance(s,config);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					e.printStackTrace();
					return null;
				}
			};
		} catch (NoSuchMethodException | SecurityException e) {
			// no config section constructor, which is fine if the appender does not have
			// any parameter, in which case it only has a constructor with the snitch as parameter
			try {
				Constructor<? extends AbstractSnitchAppender> constructor = clazz.getConstructor(Snitch.class);
				return s -> {
					try {
						return constructor.newInstance(s);
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e1) {
						e1.printStackTrace();
						return null;
					}
				};
			} catch (NoSuchMethodException | SecurityException e1) {
				// No appropriate constructor, the appender has a bug
				e1.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * Gets the configuration tied to the given ItemStack
	 * 
	 * @param is ItemStack to get configuration for
	 * @return Configuration with the given ItemStack or null if no such config
	 *         exists
	 */
	public SnitchFactoryType getConfig(ItemStack is) {
		if (is == null) {
			return null;
		}
		ItemStack copy = is.clone();
		copy.setAmount(1);
		return configFactoriesByItem.get(copy);
	}

	public SnitchFactoryType getConfig(int id) {
		return configFactoriesById.get(id);
	}

}
