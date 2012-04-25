package me.Josvth.RandomSpawn.Listeners;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.metadata.FixedMetadataValue;

import me.Josvth.RandomSpawn.RandomSpawn;

public class WorldChangeListener {
	
	RandomSpawn plugin;
	
	public WorldChangeListener(RandomSpawn instance){
		plugin = instance;
	}
	
	public void onPlayerWorldChange(PlayerChangedWorldEvent event){
			
		Player player = event.getPlayer();
		String playerName = player.getName();
				
		if (player.hasPermission("RandomSpawn.exclude")){ 																// checks if player should be excluded
			plugin.logDebug(playerName + " is excluded from Random Spawning.");
			return; 
		}
				
		World from = event.getFrom();
		World to = event.getPlayer().getWorld();
		
		List<String> randomSpawnFlags = plugin.yamlHandler.worlds.getStringList(to.getName() + ".randomspawnon");
		
		if(randomSpawnFlags.contains("from-any-world")){
			randomSpawnPlayerInWorld(event.getPlayer(), to);
		}
		
		if(to.getEnvironment().equals(Environment.NORMAL)){	// player going to a normal world
			
			World nether = plugin.getServer().getWorld(to.getName()+"_nether");
			World end = plugin.getServer().getWorld(to.getName()+"_the_end");
			
			if(from.equals(nether)) {		// player coming from own nether
				if(randomSpawnFlags.contains("teleport-from-own-nether")){
					randomSpawnPlayerInWorld(event.getPlayer(), to);
				}
			}else if(from.equals(end)){		// player coming from own end
				if(randomSpawnFlags.contains("teleport-from-own-end")){
					randomSpawnPlayerInWorld(event.getPlayer(), to);
				}
			}
		}else if(to.getEnvironment().equals(Environment.NETHER)){
			World normal = plugin.getServer().getWorld(to.getName().replaceAll("[_nether]",""));
			if(from.equals(normal)){
				if(randomSpawnFlags.contains("teleport-from-overworld")){
					randomSpawnPlayerInWorld(event.getPlayer(), to);
				}
			}
		}else if(to.getEnvironment().equals(Environment.THE_END)){
			World normal = plugin.getServer().getWorld(to.getName().replaceAll("[_the_end]",""));
			if(from.equals(normal)){
				if(randomSpawnFlags.contains("teleport-from-overworld")){
					randomSpawnPlayerInWorld(event.getPlayer(), to);
				}
			}
		}
	}
	
	public void randomSpawnPlayerInWorld(Player player, World world){
		
		Location spawnLocation = plugin.chooseSpawn(world);
		
		plugin.sendGround(player, spawnLocation);
		
		player.teleport(spawnLocation);
		
		player.setMetadata("lasttimerandomspawned", new FixedMetadataValue(plugin, System.currentTimeMillis()));
					
		if (plugin.yamlHandler.config.getString("messages.randomspawned") != null){
			player.sendMessage(plugin.yamlHandler.config.getString("messages.randomspawned"));
		}
		
	}
}
