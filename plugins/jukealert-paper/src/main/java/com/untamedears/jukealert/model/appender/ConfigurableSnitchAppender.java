package com.untamedears.jukealert.model.appender;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.appender.config.AppenderConfig;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;

public abstract class ConfigurableSnitchAppender<C extends AppenderConfig> extends AbstractSnitchAppender {

	private static Map<String, AppenderConfig> configs = new HashMap<>();

	private static AppenderConfig retrieveConfig(Class<? extends AppenderConfig> clazz, ConfigurationSection config) {
		return configs.computeIfAbsent(config.getCurrentPath(), s -> {
			try {
				return clazz.getConstructor(ConfigurationSection.class).newInstance(config);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				JukeAlert.getInstance().getLogger().log(Level.SEVERE, "Failed to instanciate config", e);
				return null;
			}
		});
	}

	protected final C config;

	@SuppressWarnings("unchecked")
	public ConfigurableSnitchAppender(Snitch snitch, ConfigurationSection configSection) {
		super(snitch);
		this.config = (C) retrieveConfig(getConfigClass(), configSection);
	}
	
	public C getConfig() {
		return config;
	}

	public abstract Class<C> getConfigClass();

}
