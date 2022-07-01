package com.programmerdan.minecraft.simpleadminhacks.configs;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHackConfig;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public class SaplingConfig extends SimpleHackConfig {

	private Map<Material, Double> chanceMap;
	private boolean allowFortune;

	public SaplingConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	public SaplingConfig(SimpleAdminHacks plugin, ConfigurationSection base, boolean wireup) {
		super(plugin, base, wireup);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		this.allowFortune = config.getBoolean("allow_fortune", false);
		this.chanceMap = new HashMap<>();
		for (String replace : config.getConfigurationSection("blocks").getKeys(false)) {
			Material leaves = Material.matchMaterial(replace.toUpperCase());
			//Incase this breaks, default to no extra drops
			Double chance = config.getConfigurationSection("blocks").getDouble(replace, 0.0);
			if (leaves != null && chance != null) {
				chanceMap.put(leaves, chance);
			}
		}
	}

	public Map<Material, Double> getChanceMap() {
		return Collections.unmodifiableMap(this.chanceMap);
	}

	public boolean isAllowFortune() {
		return this.allowFortune;
	}
}
