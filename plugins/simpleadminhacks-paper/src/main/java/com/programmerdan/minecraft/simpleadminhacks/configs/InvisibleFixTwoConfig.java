package com.programmerdan.minecraft.simpleadminhacks.configs;

import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;

/**
 * Lightweight config for InvisibleFixTwo
 *
 * @author ProgrammerDan
 */
public class InvisibleFixTwoConfig extends SimpleHackConfig {

	/**
	 * How many ticks after teleport to attempt to force a refresh of player position.
	 */
	private long teleportFixDelay;

	/**
	 * How many milliseconds in between each forced update of player position.
	 */
	private long fixInterval;

	private boolean ignoreOps;
	private String ignorePermission;

	public InvisibleFixTwoConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		this.teleportFixDelay = config.getLong("teleportDelay", 5l);
		this.fixInterval = config.getLong("minInterval", 100l);
		this.ignoreOps = config.getBoolean("ignoreOps", true);
		this.ignorePermission = config.getString("ignorePermission", "sah.allowInvisible");
		if (this.ignorePermission.equals("")) this.ignorePermission = null;
	}

	public long getTeleportFixDelay() {
		return this.teleportFixDelay;
	}

	public long getFixInterval() {
		return this.fixInterval;
	}

	public boolean getIgnoreOps() {
		return this.ignoreOps;
	}

	public String getIgnorePermission() {
		return this.ignorePermission;
	}
}

