package com.programmerdan.minecraft.simpleadminhacks.configs;

import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;

/**
 * Dumping ground for experimental stuff. Highly volatile. May explode.
 * 
 * @author ProgrammerDan
 *
 */
public class ExperimentalConfig extends SimpleHackConfig {
	private boolean combatSpy;
	public ExperimentalConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		this.combatSpy = config.getBoolean("combatspy", false);
	}

	public boolean isCombatSpy() {
		return this.combatSpy;
	}
}

