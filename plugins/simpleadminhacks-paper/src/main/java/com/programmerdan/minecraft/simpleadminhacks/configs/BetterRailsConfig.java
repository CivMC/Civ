package com.programmerdan.minecraft.simpleadminhacks.configs;

import com.google.common.collect.Maps;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHackConfig;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import vg.civcraft.mc.civmodcore.utilities.CivLogger;

import java.util.*;

public final class BetterRailsConfig extends SimpleHackConfig {

	private final CivLogger logger;

	private Map<Material, Double> speeds;
	private double baseSpeed = 8;

	private Map<Material, Double> skySpeeds;
	private double skySpeed = 0;

	public BetterRailsConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base, false);
		this.logger = CivLogger.getLogger(getClass());
		wireup(base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		this.baseSpeed = config.getDouble("base");

		ConfigurationSection materials = config.getConfigurationSection("materials");
		Set<String> keys = materials.getKeys(false);
		this.speeds = Maps.newHashMapWithExpectedSize(keys.size());
		for (String key : keys) {
			this.speeds.put(Material.valueOf(key), materials.getDouble(key));
		}

		this.skySpeed = config.getDouble("skyBase");

		ConfigurationSection skyMaterials = config.getConfigurationSection("skyMaterials");
		Set<String> skyKeys = skyMaterials.getKeys(false);
		this.skySpeeds = Maps.newHashMapWithExpectedSize(skyKeys.size());
		for (String key : skyKeys) {
			this.skySpeeds.put(Material.valueOf(key), skyMaterials.getDouble(key));
		}
	}

	public Double getMaxSpeedMetresPerSecond(Material material) {
		return speeds.get(material);
	}

	public Double getSkySpeedMetresPerSecond(Material material) {
		return skySpeeds.get(material);
	}

	public double getBaseSpeed() {
		return baseSpeed;
	}

	public double getSkySpeed() {
		return skySpeed;
	}
}
