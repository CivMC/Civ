package com.programmerdan.minecraft.simpleadminhacks.configs;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;

public class PhantomOfTheOperaConfig extends SimpleHackConfig {

	private int timeSinceRestCap;

	private boolean nightSpawn;

	private boolean stormSpawn;

	public PhantomOfTheOperaConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		// Parse the cap for TIME_SINCE_REST
		this.timeSinceRestCap = config.getInt("timeSinceRestCap", 80_000);
		if (this.timeSinceRestCap == -1) {
			plugin().log(Level.INFO, "[PhantomOfTheOpera] TIME_SINCE_REST will not be capped.");
		}
		else if (this.timeSinceRestCap < -1) {
			plugin().log(Level.WARNING, "[PhantomOfTheOpera] TIME_SINCE_REST invalid value, defaulting.");
			this.timeSinceRestCap = 80_000;
		}
		else if (this.timeSinceRestCap < 72_000) {
			plugin().log(Level.WARNING, "[PhantomOfTheOpera] TIME_SINCE_REST capped below spawning value.");
		}
		else {
			plugin().log(Level.INFO, "[PhantomOfTheOpera] TIME_SINCE_REST capped to: " + this.timeSinceRestCap);
		}
		// Parse phantom spawning at night
		this.nightSpawn = config.getBoolean("nightSpawn", true);
		if (this.nightSpawn) {
			plugin().log(Level.INFO, "[PhantomOfTheOpera] Phantoms are now allowed to spawn during the night.");
		}
		else {
			plugin().log(Level.INFO, "[PhantomOfTheOpera] Phantoms are no longer allowed to spawn during the night.");
		}
		// Parse phantom spawning during storms
		this.stormSpawn = config.getBoolean("stormSpawn", true);
		if (this.stormSpawn) {
			plugin().log(Level.INFO, "[PhantomOfTheOpera] Phantoms are now allowed to spawn during storms.");
		}
		else {
			plugin().log(Level.INFO, "[PhantomOfTheOpera] Phantoms are no longer allowed to spawn during storms.");
		}
	}

	public int getTimeSinceRestCap() {
		return timeSinceRestCap;
	}

	public boolean canNightSpawn() {
		return nightSpawn;
	}

	public boolean canStormSpawn() {
		return stormSpawn;
	}

}
