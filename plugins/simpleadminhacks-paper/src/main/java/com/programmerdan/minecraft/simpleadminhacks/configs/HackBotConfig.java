package com.programmerdan.minecraft.simpleadminhacks.configs;

import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;

/**
 * Captures the configuration for the NPCs that HackBot hack can produce.
 * 
 * @author ProgrammerDan <programmerdan@gmail.com>
 *
 */
public class HackBotConfig extends SimpleHackConfig {

	private ConfigurationSection bots;
	private boolean spawnOnLoad;

	public HackBotConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	public HackBotConfig(ConfigurationSection base) {
		super(SimpleAdminHacks.instance(), base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		bots = config.getConfigurationSection("bots");
		if (bots == null) {
			bots = config.createSection("bots");
		}
		spawnOnLoad = config.getBoolean("spawn_on_load", false);
	}

	public ConfigurationSection getBots() {
		return bots;
	}

	public boolean doSpawnOnLoad() {
		return spawnOnLoad;
	}

	public void setSpawnOnLoad(boolean spawnOnLoad) {
		if (this.isEnabled()) {
			getBase().set("spawn_on_load", spawnOnLoad);
			this.spawnOnLoad = spawnOnLoad;
		}
	}
}
