package com.programmerdan.minecraft.simpleadminhacks.configs;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;

/**
 * Holds configurations for the GameFeatures module.
 *
 * @author ProgrammerDan
 */
public class GameFeaturesConfig extends SimpleHackConfig {

	private boolean potatoXPEnabled;

	public GameFeaturesConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		this.potatoXPEnabled = !config.getBoolean("disablePotatoXP", false);
		if (!this.potatoXPEnabled) plugin().log("  Potato XP Disabled");

		/* Add additional feature config grabs here. */
	}

	/**
	 * @return If getting XP from potatos is enabled.
     */
	public boolean isPotatoXPEnabled() {
		return this.potatoXPEnabled;
	}
}

