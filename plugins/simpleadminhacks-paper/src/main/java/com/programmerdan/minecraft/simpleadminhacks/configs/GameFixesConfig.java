package com.programmerdan.minecraft.simpleadminhacks.configs;

import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;

public class GameFixesConfig extends SimpleHackConfig {

	private boolean blockElytraBreakBug;
	private double damageOnElytraBreakBug;
	private boolean canStorageTeleport;
	private boolean stopHopperDupe;
	private boolean stopRailDupe;
	private boolean stopEndPortalDeletion;
	
	public GameFixesConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}
	
	protected void wireup(ConfigurationSection config) {
		blockElytraBreakBug = config.getBoolean("blockElytraBreakBug", true);
		damageOnElytraBreakBug = config.getDouble("damageOnElytraBreakBug", 0.0d);
		canStorageTeleport = config.getBoolean("canStorageTeleport");
		stopHopperDupe = config.getBoolean("stopHopperDupe");

		stopRailDupe = config.getBoolean("stopRailDupe", true);
		if(stopRailDupe) plugin().log("Stop Rail Dupe is enabled.");

		stopEndPortalDeletion = config.getBoolean("stopEndPortalDeletion", true);
		if (stopEndPortalDeletion) plugin().log("Stop End Portal Deletion is enabled.");
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

	public boolean isStopRailDupe()
	{
		return stopRailDupe;
	}

	public boolean isStopEndPortalDeletion()
	{
		return stopEndPortalDeletion;
	}
}