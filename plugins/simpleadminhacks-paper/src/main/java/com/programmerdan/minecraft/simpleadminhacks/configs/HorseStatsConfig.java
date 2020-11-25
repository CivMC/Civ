package com.programmerdan.minecraft.simpleadminhacks.configs;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHackConfig;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public class HorseStatsConfig extends SimpleHackConfig {

	private Material horseCheckerItem;

	public HorseStatsConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		horseCheckerItem = Material.getMaterial(config.getString("wand"));
	}

	public Material getHorseCheckerItem() {
		return horseCheckerItem;
	}
}
