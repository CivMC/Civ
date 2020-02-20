package com.programmerdan.minecraft.simpleadminhacks.configs;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;
import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Level;

public class AutoRespawnConfig extends SimpleHackConfig {
	
	private static final long defaultRespawnDelay = 1000 * 60 * 5; // Five minutes
	private static final long defaultLoginRespawnDelay = 0; // Instantly
	
	private long respawnDelay;
	private long loginRespawnDelay;
	private String[] respawnQuotes;
	
	public AutoRespawnConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}
	
	@Override
	protected void wireup(ConfigurationSection config) {
		this.respawnDelay = config.getLong("respawnDelay", defaultRespawnDelay);
		if (this.respawnDelay <= 0) {
			plugin().log(Level.WARNING, "\tAuto Respawn delay has an invalid value, defaulting.");
			this.respawnDelay = defaultRespawnDelay;
		}
		else {
			plugin().log(Level.INFO, "\tAuto Respawn delay set to: " + this.respawnDelay);
		}
		this.loginRespawnDelay =config.getLong("loginRespawnDelay", defaultLoginRespawnDelay);
		if (this.loginRespawnDelay < 0) {
			plugin().log(Level.WARNING, "\tLogin Respawn delay has an invalid value, defaulting.");
			this.loginRespawnDelay = defaultLoginRespawnDelay;
		}
		else {
			plugin().log(Level.INFO, "\tLogin Respawn delay set to: " + this.loginRespawnDelay);
		}
		this.respawnQuotes = config.getStringList("respawnQuotes").toArray(new String[0]);
		if (this.respawnQuotes.length < 1) {
			plugin().log(Level.WARNING, "\tThere are no auto respawn quotes :'(");
		}
		else {
			plugin().log(Level.INFO, "\tThe following auto respawn quotes were loaded:");
			for (String quote : this.respawnQuotes) {
				plugin().log(Level.INFO, "\t\t- " + quote);
			}
		}
	}
	
	public long getRespawnDelay() {
		return this.respawnDelay;
	}
	
	public long getLoginRespawnDelay() {
		return this.loginRespawnDelay;
	}
	
	public String[] getRespawnQuotes() {
		return this.respawnQuotes;
	}
	
}
