package com.programmerdan.minecraft.simpleadminhacks.configs;

import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;

public class GameFixesConfig extends SimpleHackConfig {

	private boolean blockElytraBreakBug;
	private double damageOnElytraBreakBug;
	
	public GameFixesConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}
	
	protected void wireup(ConfigurationSection config) {
		blockElytraBreakBug = config.getBoolean("blockElytraBreakBug", true);
		damageOnElytraBreakBug = config.getDouble("damageOnElytraBreakBug", 0.0d);
	}
	
	public boolean isBlockElytraBreakBug() {
		return blockElytraBreakBug;
	}
	
	public double getDamageOnElytraBreakBug() {
		return damageOnElytraBreakBug;
	}
}
