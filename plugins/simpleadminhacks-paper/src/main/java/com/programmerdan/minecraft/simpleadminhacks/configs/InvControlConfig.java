package com.programmerdan.minecraft.simpleadminhacks.configs;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHackConfig;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Stub; this hack just turns on and off, and has some commands.
 * 
 * @author ProgrammerDan
 *
 */
public class InvControlConfig extends SimpleHackConfig {
	public InvControlConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
	}
}
