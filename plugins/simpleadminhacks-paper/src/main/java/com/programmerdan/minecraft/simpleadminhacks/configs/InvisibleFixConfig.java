package com.programmerdan.minecraft.simpleadminhacks.configs;

import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;

/**
 * Lightweight config for InvisibleFix
 *
 * @author ProgrammerDan
 */
public class InvisibleFixConfig extends SimpleHackConfig {

	private boolean ignoreOps;
	private String ignorePermission;
	private long recheckInterval;
	private int maxPlayersPerRecheck;

	public InvisibleFixConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {

		this.ignoreOps = config.getBoolean("ignoreOps", true);
		this.ignorePermission = config.getString("ignorePermission", "sah.allowInvisible");
		if (this.ignorePermission.equals("")) this.ignorePermission = null;
		this.recheckInterval = config.getLong("recheckInterval", 500l);
		this.maxPlayersPerRecheck = config.getInt("maxPlayerPerRecheck", 5);
	}

	public boolean getIgnoreOps() {
		return this.ignoreOps;
	}

	public String getIgnorePermission() {
		return this.ignorePermission;
	}

	public long getRecheckInterval() {
		return this.recheckInterval;
	}

	public int getMaxPlayersPerRecheck() {
		return this.maxPlayersPerRecheck;
	}
}
