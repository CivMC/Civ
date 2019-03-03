package com.programmerdan.minecraft.simpleadminhacks;

import java.util.logging.Level;

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
	private String broadcastPermission;

	public SimpleAdminHacksConfig(ConfigurationSection root) {
		this(SimpleAdminHacks.instance(), root);
	}

	/**
	 * Creates a new master Config based on the loaded config.
	 * 
	 * @param plugin the Hacks master
	 * @param root the configuration to use
	 */
	public SimpleAdminHacksConfig(SimpleAdminHacks plugin, ConfigurationSection root) {
		this.plugin = plugin;
		this.config = root;

		int actual_config_level = config.getInt("configuration_file_version", -1);
		if (actual_config_level < 0 || actual_config_level > SimpleAdminHacksConfig.expected_config_level) {
			throw new InvalidConfigException("Invalid configuration file version");
		}

		this.debug = config.getBoolean("debug", false);
		if (this.debug) {
			this.plugin.log("Debug messages enabled");
		}

		this.broadcastPermission = config.getString("broadcast_permission", "simpleadmin.broadcast");
		if (this.debug) {
			this.plugin.log(Level.INFO, "broadcast_permission set to {0}", this.broadcastPermission);
		}
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

	public String getBroadcastPermission() {
		return this.broadcastPermission;
	}
}
