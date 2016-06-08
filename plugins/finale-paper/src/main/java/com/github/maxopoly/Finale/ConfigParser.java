package com.github.maxopoly.finale;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import static vg.civcraft.mc.civmodcore.util.ConfigParsing.parseTime;

import com.github.maxopoly.finale.misc.SaturationHealthRegenHandler;

public class ConfigParser {
	private Finale plugin;
	private FinaleManager manager;
	private boolean pearlEnabled;
	private long pearlCooldown;
	private boolean combatTagOnPearl;
	
	public ConfigParser(Finale plugin) {
		this.plugin = plugin;
	}
	
	public FinaleManager parse() {
		plugin.info("Parsing config");
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		FileConfiguration config = plugin.getConfig();
		// Attack Speed modification for all players
		boolean attackEnabled = config.getBoolean("alterAttack.enabled", true);
		double attackSpeed = config.getDouble("alterAttack.speed", 9.4); 
		// Food Health Regen modifications for all players
		boolean regenEnabled = config.getBoolean("foodHealthRegen.enabled", false);
		SaturationHealthRegenHandler regenhandler = regenEnabled ? 
				parseHealthRegen(config.getConfigurationSection("foodHealthRegen")) : null;
		// Pearl cooldown changes
		this.pearlEnabled = parsePearls(config.getConfigurationSection("pearls"));
		// Flags
		boolean protocolLibEnabled = Bukkit.getPluginManager().isPluginEnabled("ProtocolLib");

		// Initialize the manager
		manager = new FinaleManager(attackEnabled, attackSpeed, regenEnabled, regenhandler, protocolLibEnabled);
		return manager;
	}
	
	private SaturationHealthRegenHandler parseHealthRegen(ConfigurationSection config) {
		//default values are vanilla 1.8 behavior
		int interval = (int) parseTime(config.getString("interval", "4s"));
		float exhaustionPerHeal = (float) config.getDouble("exhaustionPerHeal", 3.0);
		int minimumFood = config.getInt("minimumFood", 18);
		double healthPerCycle = config.getDouble("healthPerCycle", 1.0);
		boolean blockFoodRegen = config.getBoolean("blockFoodRegen", true);
		boolean blockSaturationRegen = config.getBoolean("blockSaturationRegen", true);
		return new SaturationHealthRegenHandler(interval, healthPerCycle, minimumFood, exhaustionPerHeal, blockSaturationRegen, blockFoodRegen);
	}
	
	private boolean parsePearls(ConfigurationSection config) {
		if (config == null || !config.getBoolean("enabled", false)) {
			return false;
		}
		pearlCooldown = parseTime(config.getString("cooldown", "10s"));
		combatTagOnPearl = config.getBoolean("combatTag", true) && Bukkit.getPluginManager().isPluginEnabled("CombatTagPlus");
		return true;
	}

	public boolean isPearlEnabled() {
		return pearlEnabled;
	}
	
	public long getPearlCoolDown() {
		return pearlCooldown;
	}
	
	public boolean combatTagOnPearl() {
		return combatTagOnPearl;
	}

}
