package com.untamedears.JukeAlert;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.plugin.java.JavaPlugin;
import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.access.AccessDelegate;

public class JukeAlertSnitch extends JavaPlugin{{
	

	
	if (Bukkit.getPluginManager().isPluginEnabled("Citadel")){  //gets citadel.
		Location location= null;
			
		if(JukeAlertListening.snitch == Citadel.getReinforcementManager().getReinforcement(location.getBlock())){
			if(JukeAlertListening.player== AccessDelegate.getDelegate(JukeAlertListening.snitch)){
				//Juke will send the player a message if someone crosses their field.  unfinished.
			}
		}
		
		
		
		
	}
	else {
		getLogger().info("Citadel is not loaded, this plugin will not function as intended.");
	}


}}
