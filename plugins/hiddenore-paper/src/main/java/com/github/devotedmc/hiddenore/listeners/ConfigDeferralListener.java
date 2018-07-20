package com.github.devotedmc.hiddenore.listeners;

import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;

import com.github.devotedmc.hiddenore.Config;
import com.github.devotedmc.hiddenore.HiddenOre;

/**
 * For world ore clearing to work, the plugin has to launch on startup.
 * This means however that worlds aren't loaded yet, which breaks the tight-binding
 * goal for world-specific configs.
 * 
 * This infrastructure allows soft-binding during first load and eventual resolution of
 * binding once the worlds are loaded.
 * 
 * @author ProgrammerDan
 */
public class ConfigDeferralListener implements Listener {

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void handleWorldInitEvent(WorldInitEvent init) {
		checkPreLoad(init.getWorld());
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void handleWorldLoadEvent(WorldLoadEvent init) {
		checkPreLoad(init.getWorld());
	}
	
	private void checkPreLoad(World world) {
		try {
			String found = null;
			if (Config.instance.preloadBlockConfigs.size() > 0) {
				for (String key : Config.instance.preloadBlockConfigs.keySet()) {
					if (world.getName().equals(key)) {
						HiddenOre.getPlugin().getLogger().log( Level.INFO, "Found a match during loading for {0}, bound config to world", key);
						Config.instance.blockConfigs.put(world.getUID(), Config.instance.preloadBlockConfigs.get(key));
						found = key;
						break;
					}
					
					try {
						UUID worldkey = UUID.fromString(key);
						if (worldkey != null && world.getUID().equals(worldkey)) {
							HiddenOre.getPlugin().getLogger().log( Level.INFO, "Found a match during loading for {0}, bound config to world", key);
							Config.instance.blockConfigs.put(world.getUID(), Config.instance.preloadBlockConfigs.get(key));
							found = key;
							break;							
						}
					} catch (IllegalArgumentException e) {
						// no match.
					}
				}
			}
			
			if (found != null) {
				Config.instance.preloadBlockConfigs.remove(found);
			}
			
		} catch (Exception e) {
			HiddenOre.getPlugin().getLogger().log(Level.WARNING, "Tried to check for dangling preloaded configs, failed", e);
		}
	}
}
