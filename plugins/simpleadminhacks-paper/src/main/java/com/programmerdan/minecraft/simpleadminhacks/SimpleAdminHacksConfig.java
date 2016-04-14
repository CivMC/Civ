package com.programmerdan.minecraft.simpleadminhacks;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Baseline configuration for SimpleAdminHacks and parser for all actual Hacks.
 *
 * @author ProgrammerDan
 */
public class SimpleAdminHacksConfig {

	private static int expected_config_level = 1;

	private SimpleAdminHacks plugin;
	private ConfigurationSection config;

	private boolean debug;

	public SimpleAdminHacksConfig(ConfigurationSection root) {
		this(SimpleAdminHacks.instance(), root);
	}

	public SimpleAdminHacksConfig(SimpleAdminHacks plugin, ConfigurationSection root) {
		this.plugin = plugin;
		this.config = root;

		int actual_config_level = config.getInt("configuration_file_version", -1);
		if (actual_config_level < 0 || actual_config_level > SimpleAdminHacksConfig.expected_config_level) {
			throw new InvalidConfigException("Invalid configuration file version");
		}

		this.debug = config.getBoolean("debug", false);

		// Now load all the Hacks and register.
		ConfigurationSection hacks = config.getSection("hacks");
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean mode) {
		this.debug = mode;
		update("debug", mode);
	}

	protected void update(String node, Object value) {
		config.set(node, value);
		plugin.saveConfig();
	}
}
