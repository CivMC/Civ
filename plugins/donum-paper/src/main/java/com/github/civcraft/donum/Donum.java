package com.github.civcraft.donum;

import com.github.civcraft.donum.commands.CustomCommandHandler;

import vg.civcraft.mc.civmodcore.ACivMod;

public class Donum extends ACivMod {

	private static Donum instance;
	private static DonumManager manager;

	public void onEnable() {
		super.onEnable();
		instance = this;
		manager = new DonumManager();
		handle = new CustomCommandHandler();
		handle.registerCommands();

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

}
