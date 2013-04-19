package me.josvth.randomspawn.listeners;

import java.util.List;

import me.josvth.randomspawn.RandomSpawn;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class RespawnListener implements Listener{
	
	RandomSpawn plugin;
	
	public RespawnListener (RandomSpawn instance){
		plugin = instance;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event){
		
		Player player = event.getPlayer();
		String playerName = player.getName();
				
		if (player.hasPermission("RandomSpawn.exclude")){ 																// checks if player should be excluded
			plugin.logDebug(playerName + " is excluded from Random Spawning.");
			return; 
		}
		
		World world = event.getRespawnLocation().getWorld();
		String worldName = world.getName();
		
		List<String> randomSpawnFlags = plugin.yamlHandler.worlds.getStringList(worldName + ".randomspawnon");
				
		if (event.isBedSpawn() && !randomSpawnFlags.contains("bedrespawn")){  		// checks if player should be spawned at his bed
			plugin.logDebug(playerName + " is spawned at his bed!");
			return; 
		}
		
		if (plugin.yamlHandler.worlds.getBoolean(worldName + ".keeprandomspawns", false) && player.getBedSpawnLocation() != null ){
			event.setRespawnLocation(player.getBedSpawnLocation());
			plugin.logDebug(playerName + " is spawned at his saved spawn.");
			return;
		}
		
		if (randomSpawnFlags.contains("respawn")){
			
			Location spawnLocation = plugin.chooseSpawn(world);
			
			//player.sendMessage("You should be random spawned at: " + spawnLocation.getX() + "," + spawnLocation.getY() + "," + spawnLocation.getZ());
			
			plugin.sendGround(player, spawnLocation);
			
			event.setRespawnLocation(spawnLocation);
			
			player.setMetadata("lasttimerandomspawned", new FixedMetadataValue(plugin, System.currentTimeMillis()));
						
			if (plugin.yamlHandler.worlds.getBoolean(worldName + ".keeprandomspawns",false)){
				player.setBedSpawnLocation(spawnLocation);
			}

			if (plugin.yamlHandler.config.getString("messages.randomspawned") != null){
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.yamlHandler.config.getString("messages.randomspawned")));
			}
		}			
	}
}
