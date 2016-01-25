package com.github.igotyou.FactoryMod;

import org.bukkit.entity.Player;

import com.github.igotyou.FactoryMod.commands.FactoryModCommandHandler;
import com.github.igotyou.FactoryMod.interactionManager.FurnCraftChestInteractionManager;
import com.github.igotyou.FactoryMod.listeners.CompactItemListener;
import com.github.igotyou.FactoryMod.listeners.FactoryModListener;
import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;
import com.github.igotyou.FactoryMod.utility.MenuBuilder;

import vg.civcraft.mc.civmenu.guides.ResponseManager;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.itemHandling.NiceNames;

public class FactoryMod extends ACivMod {
	private static FactoryModManager manager;
	private static FactoryMod plugin;
	private static MenuBuilder mb;
	private static ResponseManager rm;

	public void onEnable() {
		handle = new FactoryModCommandHandler();
		handle.registerCommands();
		super.onEnable();
		plugin = this;
		MultiBlockStructure.initializeBlockFaceMap();
		ConfigParser cp = new ConfigParser(this);
		manager = cp.parse();
		mb = new MenuBuilder();
		manager.loadFactories();
		registerListeners();
		FurnCraftChestInteractionManager.prep();
		if (getServer().getPluginManager().isPluginEnabled("CivMenu")) {
			rm = ResponseManager.getResponseManager(this);
		}
		new NiceNames().loadNames();
		info("Successfully enabled");
	}

	public void onDisable() {
		manager.shutDown();
		plugin.info("Shutting down");
	}

	public static FactoryModManager getManager() {
		return manager;
	}

	public String getPluginName() {
		return "FactoryMod";
	}

	public static FactoryMod getPlugin() {
		return plugin;
	}

	private void registerListeners() {
		plugin.getServer().getPluginManager()
				.registerEvents(new FactoryModListener(manager), plugin);
		plugin.getServer()
				.getPluginManager()
				.registerEvents(
						new CompactItemListener(manager.getCompactLore()),
						plugin);
	}

	public static MenuBuilder getMenuBuilder() {
		return mb;
	}

	/**
	 * Sends a CivMenu response
	 */
	public static void sendResponse(String event, Player p) {
		if (rm != null) {
			rm.sendMessageForEvent(event, p);
		}
	}
}
