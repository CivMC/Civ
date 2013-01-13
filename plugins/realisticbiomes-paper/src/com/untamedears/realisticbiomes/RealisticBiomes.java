package com.untamedears.realisticbiomes;

import java.util.logging.Logger;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.untamedears.realisticbiomes.listener.GrowListener;
import com.untamedears.realisticbiomes.listener.SpawnListener;

public class RealisticBiomes extends JavaPlugin {

	private static final Logger LOG = Logger.getLogger("RealisticBiomes");

	public void onEnable() {
		registerEvents();
		LOG.info("[RealisticBiomes] is now enabled.");
	}

	public void onDisable() {
		LOG.info("[RealisticBiomes] is now disabled.");
	}

	private void registerEvents() {
		try {
            PluginManager pm = getServer().getPluginManager();
            pm.registerEvents(new GrowListener(), this);
            pm.registerEvents(new SpawnListener(), this);
        }
        catch(Exception e)
        {
        	LOG.warning("[RealisticBiomes] caught an exception while attempting to register events with the PluginManager");
        	e.printStackTrace();
        }
	}

}
