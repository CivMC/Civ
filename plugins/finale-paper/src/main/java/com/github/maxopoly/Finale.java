package com.github.maxopoly;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import com.github.maxopoly.external.CombatTagPlusManager;
import com.github.maxopoly.external.ProtocolLibManager;
import com.github.maxopoly.listeners.PearlCoolDownListener;
import com.github.maxopoly.listeners.PlayerListener;

import vg.civcraft.mc.civmodcore.ACivMod;

public class Finale extends ACivMod {

	private static Finale instance;
	private static FinaleManager manager;
	private static CombatTagPlusManager ctpManager;
	private static ProtocolLibManager protocolLibManager;
	
	private ConfigParser config;

	public void onEnable() {
		instance = this;
		config = new ConfigParser(this);
		manager = config.parse();
		initExternalManagers();
		registerListener();
	}

	public void onDisable() {

	}

	public static Finale getPlugin() {
		return instance;
	}

	public static FinaleManager getManager() {
		return manager;
	}

	public static CombatTagPlusManager getCombatTagPlusManager() {
		return ctpManager;
	}
	
	public static ProtocolLibManager getProtocolLibManager() {
		return protocolLibManager;
	}

	public String getPluginName() {
		return "Finale";
	}

	private void registerListener() {
		Bukkit.getPluginManager().registerEvents(new PlayerListener(manager),
				this);
		Bukkit.getPluginManager().registerEvents(
				new PearlCoolDownListener(config.getPearlCoolDown(), config.combatTagOnPearl(), ctpManager), this);
	}

	private void initExternalManagers() {
		PluginManager plugins = Bukkit.getPluginManager();
		if (plugins.isPluginEnabled("CombatTagPlus")) {
			ctpManager = new CombatTagPlusManager();
		}
		if (plugins.isPluginEnabled("ProtocolLib")) {
			protocolLibManager = new ProtocolLibManager();
		}
	}

}
