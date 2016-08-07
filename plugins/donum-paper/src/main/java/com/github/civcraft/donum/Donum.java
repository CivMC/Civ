package com.github.civcraft.donum;

import org.bukkit.Bukkit;

import com.github.civcraft.donum.commands.DonumCommandHandler;
import com.github.civcraft.donum.listeners.AdminDeliveryListener;
import com.github.civcraft.donum.listeners.PlayerListener;

import vg.civcraft.mc.civmodcore.ACivMod;

public class Donum extends ACivMod {

	private static Donum instance;
	private static DonumManager manager;
	private static DonumConfiguration config;

	public void onEnable() {
		super.onEnable();
		instance = this;
		config = new DonumConfiguration();
		config.parse();
		manager = new DonumManager();
		handle = new DonumCommandHandler();
		handle.registerCommands();
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getPluginManager().registerEvents(new AdminDeliveryListener(), this);
	}

	public void onDisable() {

	}

	@Override
	protected String getPluginName() {
		return "Donum";
	}

	public static Donum getInstance() {
		return instance;
	}

	public static DonumManager getManager() {
		return manager;
	}
	
	public static DonumConfiguration getConfiguration() {
		return config;
	}

}
