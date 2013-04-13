package com.untamedears.JukeAlert;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.plugin.java.JavaPlugin;
import com.untamedears.citadel.Citadel;

public class JukeAlertSnitch extends JavaPlugin{{
	private juke;

	
	if (Bukkit.getPluginManager().isPluginEnabled("Citadel")){  //gets citadel.
		Location location= null;
		Citadel.getReinforcementManager().getReinforcement(location.getBlock());	//gets the reinforcement block.
		
		
		
		
		
	}
	else {
		getLogger().info("Citadel is not loaded, this plugin will not function as intended.");
	}


}}
