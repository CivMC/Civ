package com.github.igotyou.FactoryMod;

import com.github.igotyou.FactoryMod.listeners.CitadelListener;
import com.github.igotyou.FactoryMod.listeners.CompactItemListener;
import com.github.igotyou.FactoryMod.listeners.FactoryModListener;
import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;
import com.github.igotyou.FactoryMod.utility.MenuBuilder;

import vg.civcraft.mc.civmodcore.ACivMod;

public class FactoryMod extends ACivMod {
	private FactoryModManager manager;
	private static FactoryMod plugin;
	private MenuBuilder menuBuilder;

	@Override
	public void onEnable() {
		super.onEnable();
		plugin = this;
		MultiBlockStructure.initializeBlockFaceMap();
		ConfigParser cp = new ConfigParser(this);
		manager = cp.parse();
		menuBuilder = new MenuBuilder(cp.getDefaultMenuFactory());
		manager.loadFactories();
		registerListeners();
		info("Successfully enabled");
	}

	@Override
	public void onDisable() {
		manager.shutDown();
		plugin.info("Shutting down");
	}

	public FactoryModManager getManager() {
		return manager;
	}

	public static FactoryMod getInstance() {
		return plugin;
	}

	private void registerListeners() {
		plugin.getServer().getPluginManager()
				.registerEvents(new FactoryModListener(manager), plugin);
		plugin.getServer()
				.getPluginManager()
				.registerEvents(
						new CompactItemListener(), plugin);
		if (manager.isCitadelEnabled()) {
			plugin.getServer().getPluginManager().registerEvents(new CitadelListener(), plugin);
		}
	}

	public MenuBuilder getMenuBuilder() {
		return menuBuilder;
	}
}
