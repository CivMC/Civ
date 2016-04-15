package com.programmerdan.minecraft.simpleadminhacks;

import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.configs.CTAnnounceConfig;
import com.programmerdan.minecraft.simpleadminhacks.hacks.CTAnnounce;

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

	public SimpleAdminHacksConfig(SimpleAdminHacks plugin, ConfigurationSection root) {
		this.plugin = plugin;
		this.config = root;

		int actual_config_level = config.getInt("configuration_file_version", -1);
		if (actual_config_level < 0 || actual_config_level > SimpleAdminHacksConfig.expected_config_level) {
			throw new InvalidConfigException("Invalid configuration file version");
		}

		this.debug = config.getBoolean("debug", false);
		
		this.broadcastPermission = config.getString("broadcast_permission", "simpleadmin.broadcast");

		// Now load all the Hacks and register.
		ConfigurationSection hacks = config.getConfigurationSection("hacks");
		for (String key : hacks.getKeys(false)) {
			ConfigurationSection hack = hacks.getConfigurationSection(key);
			
			// TODO eventually, replace this with reflection based load. For tonight, hack it.
			SimpleHack<?> newHack = bootstrapHack(hack);
			plugin.register(newHack);
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
	
	private SimpleHack<?> bootstrapHack(ConfigurationSection boot) {
		String hackName = boot.getString("name");
		if (hackName == null) {
			throw new InvalidConfigException("Hack stubbed but config lacks a name, cannot determine which hack to load");
		}
		
		try {
			if (hackName.equals(CTAnnounce.NAME)){
				return new CTAnnounce(this.plugin, new CTAnnounceConfig(boot));
			}
		} catch (InvalidConfigException ice) {
			plugin.debug("Failed to activate CTAccouncement hack");
		}
			
		throw new InvalidConfigException("Claimed to be a viable hack but isn't: " + hackName);
	}
}
