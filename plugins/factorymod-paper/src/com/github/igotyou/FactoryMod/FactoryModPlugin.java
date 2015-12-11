package com.github.igotyou.FactoryMod;

import com.github.igotyou.FactoryMod.listeners.FactoryModListener;
import com.github.igotyou.FactoryMod.multiBlockStructures.MultiBlockStructure;

import vg.civcraft.mc.civmodcore.ACivMod;

public class FactoryModPlugin extends ACivMod {
	private static FactoryModManager manager;
	private static FactoryModPlugin plugin;

	public void onEnable() {
		super.onEnable();
		plugin = this;
		ConfigParser cp = new ConfigParser(this);
		manager = cp.parse();
		MultiBlockStructure.initiliazeBlockSides();
		plugin.getServer().getPluginManager().registerEvents(new FactoryModListener(manager),plugin);
	}

	public void onDisable() {
	}

	public static FactoryModManager getManager() {
		return manager;
	}

	public String getPluginName() {
		return "FactoryMod";
	}

	public static FactoryModPlugin getPlugin() {
		return plugin;
	}
}
