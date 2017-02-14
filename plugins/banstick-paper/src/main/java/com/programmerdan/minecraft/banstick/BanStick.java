package com.programmerdan.minecraft.banstick;

import com.programmerdan.minecraft.banstick.handler.BanStickCheckHandler;
import com.programmerdan.minecraft.banstick.handler.BanStickCommandHandler;
import com.programmerdan.minecraft.banstick.handler.BanStickDatabaseHandler;
import com.programmerdan.minecraft.banstick.handler.BanStickEventHandler;

import vg.civcraft.mc.civmodcore.ACivMod;

public class BanStick extends ACivMod {
	private static BanStick instance;
	private BanStickCommandHandler commandHandler;
	private BanStickCheckHandler checkHandler;
	private BanStickEventHandler eventHandler;
	private BanStickDatabaseHandler databaseHandler;
	
	@Override
	public void onEnable() {
		super.onEnable();

		saveDefaultConfig();
		reloadConfig();
		
		BanStick.instance = this;
		connectDatabase();
		if (!this.isEnabled()) return;

		registerEventHandler();
		registerCheckHandler();
		registerCommandHandler();

		
	}
	
	private void connectDatabase() {
		try {
			this.databaseHandler = new BanStickDatabaseHandler(getConfig());
		} catch (Exception e) {
			this.severe("Failed to establish database", e);
			this.setEnabled(false);
		}
	}

	private void registerCommandHandler() {
		if (!this.isEnabled()) return;
		try {
			this.commandHandler = new BanStickCommandHandler(getConfig());
		} catch (Exception e) {
			this.severe("Failed to set up command handling", e);
			this.setEnabled(false);
		}
	}

	private void registerCheckHandler() {
		if (!this.isEnabled()) return;
		try {
			this.checkHandler = new BanStickCheckHandler(getConfig());
		} catch (Exception e) {
			this.severe("Failed to set up VPN/VPS check and delayed ban handling", e);
			this.setEnabled(false);
		}
	}

	private void registerEventHandler() {
		if (!this.isEnabled()) return;
		try {
			this.eventHandler = new BanStickEventHandler(getConfig());
		} catch (Exception e) {
			this.severe("Failed to set up event capture / handling", e);
			this.setEnabled(false);
		}	
	}

	/**
	 * 
	 * @return the static global instance. Not my fav pattern, but whatever.
	 */
	public static BanStick getPlugin() {
		return BanStick.instance;
	}
	
	/**
	 * 
	 * @return the name of this plugin.
	 */
	@Override
	protected String getPluginName() {
		return "BanStick";
	}

}
