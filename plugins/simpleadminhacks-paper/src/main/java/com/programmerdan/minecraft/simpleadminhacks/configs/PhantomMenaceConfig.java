package com.programmerdan.minecraft.simpleadminhacks.configs;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHackConfig;
import org.bukkit.configuration.ConfigurationSection;

public class PhantomMenaceConfig extends SimpleHackConfig {

	private int timeSinceRestCap;

	private boolean nightSpawn;

	private boolean stormSpawn;

	private boolean persistentChance;

	public PhantomMenaceConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		// Parse the cap for TIME_SINCE_REST
		this.timeSinceRestCap = config.getInt("timeSinceRestCap", 80_000);
		if (this.timeSinceRestCap == -1) {
			plugin().info("[PhantomMenace] TIME_SINCE_REST will not be capped.");
		}
		else if (this.timeSinceRestCap < -1) {
			plugin().warning("[PhantomMenace] TIME_SINCE_REST invalid value, defaulting.");
			this.timeSinceRestCap = 80_000;
		}
		else if (this.timeSinceRestCap < 72_000) {
			plugin().warning("[PhantomMenace] TIME_SINCE_REST capped below spawning value.");
		}
		else {
			plugin().info("[PhantomMenace] TIME_SINCE_REST capped to: " + this.timeSinceRestCap);
		}
		// Parse phantom spawning at night
		this.nightSpawn = config.getBoolean("nightSpawn", true);
		if (this.nightSpawn) {
			plugin().info("[PhantomMenace] Phantoms are now allowed to spawn during the night.");
		}
		else {
			plugin().info("[PhantomMenace] Phantoms are no longer allowed to spawn during the night.");
		}
		// Parse phantom spawning during storms
		this.stormSpawn = config.getBoolean("stormSpawn", true);
		if (this.stormSpawn) {
			plugin().info("[PhantomMenace] Phantoms are now allowed to spawn during storms.");
		}
		else {
			plugin().info("[PhantomMenace] Phantoms are no longer allowed to spawn during storms.");
		}
		// Parse phantom spawning during storms
		this.persistentChance = config.getBoolean("persistentChance", false);
		if (this.persistentChance) {
			plugin().info("[PhantomMenace] TIME_SINCE_REST is persistent.");
		}
		else {
			plugin().info("[PhantomMenace] TIME_SINCE_REST is not persistent.");
		}
	}

	public int getTimeSinceRestCap() {
		return this.timeSinceRestCap;
	}

	public boolean canNightSpawn() {
		return this.nightSpawn;
	}

	public boolean canStormSpawn() {
		return this.stormSpawn;
	}

	public boolean isChangePersistent() {
		return this.persistentChance;
	}

}
