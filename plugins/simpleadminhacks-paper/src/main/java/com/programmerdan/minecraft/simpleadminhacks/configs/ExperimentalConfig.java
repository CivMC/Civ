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
	private boolean teleportSpy;
	private boolean postTeleportSpy;
	private int postTeleportSpyCount;
	public ExperimentalConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		this.combatSpy = config.getBoolean("combatspy", false);
		this.teleportSpy = config.getBoolean("teleportspy", false);
		this.postTeleportSpy = config.getBoolean("postteleport.spy", false);
		this.postTeleportSpyCount = config.getInt("postteleport.count", 10);
	}

	public boolean isCombatSpy() {
		return this.combatSpy;
	}
	public boolean isTeleportSpy() {
		return this.teleportSpy;
	}
	public boolean isPostTeleportSpy() {
		return this.postTeleportSpy;
	}

	public int getPostTeleportSpyCount() {
		return this.postTeleportSpyCount;
	}
}

