package com.github.maxopoly;

import org.bukkit.Bukkit;

import com.github.maxopoly.listeners.PlayerListener;

import vg.civcraft.mc.civmodcore.ACivMod;

public class Finale extends ACivMod {
	
	private static Finale instance;
	private static FinaleManager manager;
	
	public void onEnable() {
		instance = this;
		ConfigParser cp = new ConfigParser(this);
		manager = cp.parse();
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
	
	public String getPluginName() {
		return "Finale";
	}
	
	private void registerListener() {
		Bukkit.getPluginManager().registerEvents(new PlayerListener(manager), this);
	}

}
