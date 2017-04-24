package com.programmerdan.minecraft.simpleadminhacks.configs;

import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;

/**
 * CrazyCrates event hook hack
 * 
 * @author ProgrammerDan
 *
 */
public class CrazyCratesAnnounceConfig extends SimpleHackConfig {
	
	public CrazyCratesAnnounceConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}
	
	public CrazyCratesAnnounceConfig(ConfigurationSection base) {
		super(SimpleAdminHacks.instance(), base);
	}
	
	@Override
	protected void wireup(ConfigurationSection config) {
		// noop
	}
}
