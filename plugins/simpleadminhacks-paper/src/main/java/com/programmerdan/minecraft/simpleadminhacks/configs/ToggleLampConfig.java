package com.programmerdan.minecraft.simpleadminhacks.configs;

import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;

public class ToggleLampConfig extends SimpleHackConfig {

	private long cooldownTime;
	
	public ToggleLampConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		this.cooldownTime = config.getLong("cooldownTime", 100);
	}
	
	public long getCooldownTime() {
		return this.cooldownTime;
	}

}
