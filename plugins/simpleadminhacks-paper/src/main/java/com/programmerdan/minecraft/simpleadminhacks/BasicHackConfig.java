package com.programmerdan.minecraft.simpleadminhacks;

import org.bukkit.configuration.ConfigurationSection;

public class BasicHackConfig extends SimpleHackConfig {

	public BasicHackConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		//not needed
	}
	
	public static BasicHackConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new BasicHackConfig(plugin, config);
	}

}
