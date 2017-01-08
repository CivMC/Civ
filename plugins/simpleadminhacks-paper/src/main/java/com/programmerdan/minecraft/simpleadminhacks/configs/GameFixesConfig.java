package com.programmerdan.minecraft.simpleadminhacks.configs;

import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;

public class GameFixesConfig extends SimpleHackConfig {

	private boolean blockElytraBreakBug;
	private double damageOnElytraBreakBug;
	private boolean canStorageTeleport;
	private boolean stopHopperDupe;
	
	public GameFixesConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}
	
	protected void wireup(ConfigurationSection config) {
		blockElytraBreakBug = config.getBoolean("blockElytraBreakBug", true);
		damageOnElytraBreakBug = config.getDouble("damageOnElytraBreakBug", 0.0d);
		canStorageTeleport = config.getBoolean("canStorageTeleport");
		stopHopperDupe = config.getBoolean("stopHopperDupe");
	}
	
	public boolean isBlockElytraBreakBug() {
		return blockElytraBreakBug;
	}
	
	public double getDamageOnElytraBreakBug() {
		return damageOnElytraBreakBug;
	}
	
	public boolean canStorageTeleport() {
		return canStorageTeleport;
	}
	
	public boolean isStopHopperDupe() {
		return stopHopperDupe;
	}
}
