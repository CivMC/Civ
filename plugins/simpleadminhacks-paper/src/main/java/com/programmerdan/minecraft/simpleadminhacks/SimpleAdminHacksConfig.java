package com.programmerdan.minecraft.simpleadminhacks;

import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.configs.CTAnnounceConfig;
import com.programmerdan.minecraft.simpleadminhacks.configs.HackBotConfig;
import com.programmerdan.minecraft.simpleadminhacks.configs.IntrobookConfig;
import com.programmerdan.minecraft.simpleadminhacks.configs.InvControlConfig;
import com.programmerdan.minecraft.simpleadminhacks.configs.NewfriendAssistConfig;
import com.programmerdan.minecraft.simpleadminhacks.hacks.CTAnnounce;
import com.programmerdan.minecraft.simpleadminhacks.hacks.HackBot;
import com.programmerdan.minecraft.simpleadminhacks.hacks.Introbook;
import com.programmerdan.minecraft.simpleadminhacks.hacks.InvControl;
import com.programmerdan.minecraft.simpleadminhacks.hacks.NewfriendAssist;

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

		// Now load all the Hacks and register.
		ConfigurationSection hacks = config.getConfigurationSection("hacks");
		for (String key : hacks.getKeys(false)) {
			ConfigurationSection hack = hacks.getConfigurationSection(key);
			
			// TODO eventually, replace this with reflection based load. For tonight, hack it.
			try {
				SimpleHack<?> newHack = bootstrapHack(hack);
				plugin.register(newHack);
				if (this.debug) {
					this.plugin.log(Level.INFO, "Registered a new hack: {0}", key);
				}
			} catch (InvalidConfigException ice) {
				this.plugin.log(Level.WARNING, key + " could not be mapped to a Hack, config values problem", ice);
			}
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
			if (hackName.equals(CTAnnounce.NAME)) {
				return new CTAnnounce(this.plugin, new CTAnnounceConfig(this.plugin, boot));
			} else if (hackName.equals(NewfriendAssist.NAME)) {
				return new NewfriendAssist(this.plugin, new NewfriendAssistConfig(this.plugin, boot));
			} else if (hackName.equals(Introbook.NAME)) {
				return new Introbook(this.plugin, new IntrobookConfig(this.plugin, boot));
			} else if (hackName.equals(HackBot.NAME)) {
				return new HackBot(this.plugin, new HackBotConfig(this.plugin, boot));
			} else if (hackName.equals(InvControl.NAME)) {
				return new InvControl(this.plugin, new InvControlConfig(this.plugin, boot));
			}
		} catch (InvalidConfigException ice) {
			plugin.log(Level.WARNING, "Failed to activate " + hackName + " hack");
		}
			
		throw new InvalidConfigException("Claimed to be a viable hack but isn't: " + hackName);
	}
}
