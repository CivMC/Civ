package me.Josvth.RandomSpawn.Listeners;

import java.util.List;

import me.Josvth.RandomSpawn.RandomSpawn;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

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
		
		World world = player.getWorld();
		String worldName = world.getName();
		
		List<String> randomSpawnFlags = plugin.yamlHandler.worlds.getStringList(worldName + ".randomspawnon");
				
		if (event.isBedSpawn() && !randomSpawnFlags.contains("bedrespawn")){  		// checks if player should be spawned at his bed
			plugin.logDebug(playerName + " is spawned at his bed!");
			return; 
		}
		
		if (plugin.yamlHandler.worlds.getBoolean(worldName + ".keeprandomspawns", false) && 
				player.hasMetadata(player.getWorld().getName() + ".spawn")){
			event.setRespawnLocation(plugin.getPlayerSpawn(player, world));
			plugin.logDebug(playerName + " is spawned at his saved spawn.");
			return;
		}
		
		if (randomSpawnFlags.contains("respawn")){
			
			Location spawnLocation = plugin.chooseSpawn(world);
			
			plugin.sendGround(player, spawnLocation);
			
			event.setRespawnLocation(spawnLocation);
			
			if (plugin.yamlHandler.worlds.getBoolean(worldName + ".keeprandomspawns",false)){
				plugin.setPlayerSpawn(player, spawnLocation);
			}
			
		}			
	}
	
//	private Location getPlayerSpawn(Player player, World world) {
//		
//		String playerName = player.getName();
//		String worldName  = world.getName();
//		
//		if (plugin.yamlHandler.spawnLocations.contains(worldName + "." + playerName)){
//			
//			double x = this.plugin.yamlHandler.spawnLocations.getDouble(worldName + "." + playerName + ".x");
//			double y = this.plugin.yamlHandler.spawnLocations.getDouble(worldName + "." + playerName + ".y");
//			double z = this.plugin.yamlHandler.spawnLocations.getDouble(worldName + "." + playerName + ".z");
//			
//			plugin.logDebug(playerName + "'s saved spawn found!");
//			
//			return new Location(world, x,y,z);
//		}
//		
//		plugin.logDebug(playerName + "'s saved spawn not found!");
//		return null;
//	}
//	
//	private void savePlayerSpawn(Player player, Location location){
//		
//		String playerName = player.getName();
//		String worldName  = location.getWorld().getName();
//		
//		plugin.yamlHandler.spawnLocations.set(worldName + "." + playerName + ".x", location.getX());
//		plugin.yamlHandler.spawnLocations.set(worldName + "." + playerName + ".y", location.getY());
//		plugin.yamlHandler.spawnLocations.set(worldName + "." + playerName + ".z", location.getZ());
//		plugin.yamlHandler.saveSpawnLocations();
//		
//		plugin.logDebug(playerName + "'s Random Spawn location is saved!");
//		
//	}
	
}
