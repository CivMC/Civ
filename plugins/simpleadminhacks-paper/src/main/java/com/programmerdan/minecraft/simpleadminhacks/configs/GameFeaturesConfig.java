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
	private boolean villagerTrading;
	private boolean witherSpawning;
	private boolean enderChestPlacement;

	private boolean weepingAngel;
	private int weepingAngelEnv;
	private int weepingAngelPlayer;

	public GameFeaturesConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		this.potatoXPEnabled = !config.getBoolean("disablePotatoXP", false);
		if (!this.potatoXPEnabled) plugin().log("  Potato XP Disabled");

		this.villagerTrading = config.getBoolean("villagerTrading", false);
		if(!villagerTrading) plugin().log("VillagerTrading is disabled");

		this.witherSpawning = config.getBoolean("witherSpawning", false);
		if (!this.witherSpawning) plugin().log("Wither Spawning is disabled");

		this.enderChestPlacement = config.getBoolean("enderChestPlacement", false);
		if (!this.enderChestPlacement) plugin().log("Placeing EnderChests is disabled");

		this.weepingAngel = config.getBoolean("weepingAngel.enabled", false);
		if(this.weepingAngel)
		{
			this.weepingAngelEnv = config.getInt("weepingAngel.enviorment", 1);
			this.weepingAngelPlayer = config.getInt("weepingAngel.playerKill", 5);

			plugin().log("Weeping Angel is enabled. Times | Env[" + weepingAngelEnv + "] PK[" + weepingAngelPlayer + "]");
		}


		/* Add additional feature config grabs here. */
	}

	/**
	 * @return If getting XP from potatos is enabled.
     */
	public boolean isPotatoXPEnabled() {
		return this.potatoXPEnabled;
	}

	public boolean isVillagerTrading()
	{
		return this.villagerTrading;
	}

	public boolean isWitherSpawning()
	{
		return this.witherSpawning;
	}

	public boolean isEnderChestPlacement()
	{
		return this.enderChestPlacement;
	}

	public boolean isWeepingAngel()
	{
		return this.weepingAngel;
	}

	public int getWeepingAngelEnv()
	{
		return this.weepingAngelEnv;
	}

	public int getWeepingAngelPlayer()
	{
		return this.weepingAngelPlayer;
	}

}

