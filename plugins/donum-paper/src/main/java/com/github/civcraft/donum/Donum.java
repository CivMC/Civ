package com.github.civcraft.donum;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.github.civcraft.donum.commands.DonumCommandHandler;
import com.github.civcraft.donum.listeners.AdminDeliveryListener;
import com.github.civcraft.donum.listeners.PlayerListener;
import com.github.civcraft.donum.listeners.storage.BukkitListener;

import vg.civcraft.mc.civmodcore.ACivMod;

public class Donum extends ACivMod {

	private static Donum instance;
	private DonumManager manager;
	private DonumConfiguration config;

	public void onEnable() {
		super.onEnable();
		instance = this;
		config = new DonumConfiguration();
		config.parse();
		manager = new DonumManager();
		handle = new DonumCommandHandler();
		handle.registerCommands();
		registerListeners();
	}

	public void onDisable() {
		for(Player p : Bukkit.getOnlinePlayers()) {
			manager.savePlayerData(p.getUniqueId(), p.getInventory(), false);
		}
	}
	
	private void registerListeners() {
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getPluginManager().registerEvents(new AdminDeliveryListener(), this);
		Bukkit.getPluginManager().registerEvents(new BukkitListener(), this);
	}

	@Override
	protected String getPluginName() {
		return "Donum";
	}

	public static Donum getInstance() {
		return instance;
	}

	public static DonumManager getManager() {
		return getInstance().manager;
	}
	
	public static DonumConfiguration getConfiguration() {
		return getInstance().config;
	}

}
