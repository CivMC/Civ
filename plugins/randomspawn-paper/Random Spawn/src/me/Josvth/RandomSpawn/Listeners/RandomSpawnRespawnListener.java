package me.Josvth.RandomSpawn.Listeners;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import me.Josvth.RandomSpawn.RandomSpawn;

public class RandomSpawnRespawnListener implements Listener {
	
	private RandomSpawn plugin;
		
	public RandomSpawnRespawnListener(RandomSpawn instance){
		plugin = instance;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event){
				
		Player player = event.getPlayer();
		String playerName = player.getName();
		
		World world = player.getWorld();
		String worldName  = world.getName();
		
		Location deathLocation = player.getLocation();
				
		if (player.hasPermission("RandomSpawn.exclude")){ 																// checks if player should be excluded
			this.plugin.logDebug(playerName + " is excluded from Random Spawning.");
			return; 
			} 							
		
		if (event.isBedSpawn() && this.plugin.yamlHandler.worlds.getBoolean(worldName + ".usebeds", true)){  		// checks if player should be spawned at his bed
			this.plugin.logDebug(playerName + " is spawned at his bed!");
			return; 
			}
		
		if (this.plugin.yamlHandler.worlds.getBoolean(worldName + ".keeprandomspawns", false)){					// checks if player should be spawned at a saved spawn
			Location savedSpawn = getPlayerSpawn(player, world);
			if (savedSpawn != null){
				event.setRespawnLocation(savedSpawn);
				this.plugin.logDebug(playerName + " is spawned at his saved spawn.");
				return;
			}
		}
		
		if (this.plugin.yamlHandler.worlds.getBoolean(worldName + ".randomspawnenabled", false)){					// checks if Random Spawn is enabled in this world
				
			Location skyLocation = new Location(deathLocation.getWorld(), deathLocation.getX(), +10000.0, deathLocation.getY());
			event.setRespawnLocation(skyLocation);	
			this.plugin.logDebug(playerName + " is teleported somewhere in the sky.");
			
			plugin.randomSpawnPlayer(player, world);
			
		}
	}
	
	private Location getPlayerSpawn(Player player, World world) {
		
		String playerName = player.getName();
		String worldName  = world.getName();
		
		if (this.plugin.yamlHandler.spawnLocations.contains(worldName + "." + playerName)){
			
			double x = this.plugin.yamlHandler.spawnLocations.getDouble(worldName + "." + player.getName() + ".x");
			double y = this.plugin.yamlHandler.spawnLocations.getDouble(worldName + "." + player.getName() + ".y");
			double z = this.plugin.yamlHandler.spawnLocations.getDouble(worldName + "." + player.getName() + ".z");
			
			this.plugin.logDebug(playerName + "'s saved spawn found!");
			
			return new Location(world, x,y,z);
		}
		
		this.plugin.logDebug(playerName + "'s saved spawn not found!");
		return null;
	}
}
