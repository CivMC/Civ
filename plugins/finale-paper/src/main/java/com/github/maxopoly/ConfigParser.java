package com.github.maxopoly;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import static vg.civcraft.mc.civmodcore.util.ConfigParsing.parseTime;

import com.github.maxopoly.misc.SaturationHealthRegenHandler;

public class ConfigParser {
	private Finale plugin;
	private FinaleManager manager;
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
		double attackSpeed = config.getDouble("attackSpeed", 9.4); 
		SaturationHealthRegenHandler regenhandler = parseHealthRegen(config.getConfigurationSection("foodHealthRegen"));
		parsePearls(config.getConfigurationSection("pearls"));
		boolean protocolLibEnabled = Bukkit.getPluginManager().isPluginEnabled("ProtocolLib");
		manager = new FinaleManager(attackSpeed, regenhandler, protocolLibEnabled);
		return manager;
	}
	
	private SaturationHealthRegenHandler parseHealthRegen(ConfigurationSection config) {
		//default values are vanilla 1.8 behavior
		int intervall = (int) parseTime(config.getString("intervall", "4s"));
		float exhaustionPerHeal = (float) config.getDouble("exhaustionPerHeal", 3.0);
		int minimumFood = config.getInt("minimumFood", 18);
		double healthPerCycle = config.getDouble("healthPerCycle", 1.0);
		boolean blockFoodRegen = config.getBoolean("blockFoodRegen", true);
		boolean blockSaturationRegen = config.getBoolean("blockSaturationRegen", true);
		return new SaturationHealthRegenHandler(intervall, healthPerCycle, minimumFood, exhaustionPerHeal, blockSaturationRegen, blockFoodRegen);
	}
	
	private void parsePearls(ConfigurationSection config) {
		if (config == null) {
			return;
		}
		pearlCooldown = parseTime(config.getString("cooldown", "10s"));
		combatTagOnPearl = config.getBoolean("combatTag", true);
	}
	
	public long getPearlCoolDown() {
		return pearlCooldown;
	}
	
	public boolean combatTagOnPearl() {
		return combatTagOnPearl;
	}

}
