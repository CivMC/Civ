package com.untamedears.JukeAlert.external;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import vg.civcraft.mc.mercury.MercuryAPI;

public class Mercury {

	private PluginManager pluginManager_;
	private boolean isEnabled = false;
	private static String[] chans = {
			"jukealert-login", "jukealert-logout", "jukealert-entry"
	};

	public Mercury(){
		pluginManager_ = Bukkit.getPluginManager();
		isEnabled = pluginManager_.isPluginEnabled("Mercury");
	}

	public boolean isEnabled(){
		return isEnabled;
	}

	public void sendMessage(String message, String... channels){
		MercuryAPI.sendGlobalMessage(message, channels);
	}

	public static String[] getChannels(){
		return chans;
	}
}
