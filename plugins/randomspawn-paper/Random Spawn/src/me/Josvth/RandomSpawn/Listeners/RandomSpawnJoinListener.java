package me.Josvth.RandomSpawn.Listeners;

import java.io.File;

import me.Josvth.RandomSpawn.RandomSpawn;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class RandomSpawnJoinListener implements Listener {

	private RandomSpawn plugin;

	public RandomSpawnJoinListener(RandomSpawn instance) {
		this.plugin = instance;
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){

		Player player = event.getPlayer();
		String playerName = player.getName();
		World world = player.getWorld();
		String worldName  = world.getName();

		File file = new File(world.getWorldFolder() + File.separator + "players" + File.separator + player.getName() + ".dat");

		if(file.exists()){ return; }																// checks if its not the first join of a player
		
		plugin.logDebug(playerName + "'s first join in " + worldName);
		
		if (plugin.getYamlHandler().worlds.getBoolean(worldName + ".randomspawnonfirstjoin",true) == false){		
			player.teleport(getFirstSpawn(world));
			plugin.logDebug(playerName + " is teleported to the first spawn of " + worldName);
			return;
		}

		if (player.hasPermission("RandomSpawn.exclude")){ return; } 							// checks if player should be excluded

		if (this.plugin.getYamlHandler().worlds.getBoolean(worldName + ".randomspawnenabled", false)){					// checks if Random Spawn is enabled in this world

			Location worldSpawn = world.getSpawnLocation();
			
			Location skyLocation = new Location(worldSpawn.getWorld(), worldSpawn.getX(), +10000.0, worldSpawn.getY());
			player.teleport(skyLocation);	
						
			this.plugin.logDebug(player.getName() + " is teleported somewhere in the sky.");
			
			plugin.randomSpawnPlayer(player, world);
		}

	}

	private Location getFirstSpawn(World world) {
		String worldName = world.getName();

		if (plugin.getYamlHandler().worlds.contains(worldName +".firstspawn")){

			double x = this.plugin.getYamlHandler().worlds.getDouble(worldName+".firstspawn.x");
			double y = this.plugin.getYamlHandler().worlds.getDouble(worldName+".firstspawn.y");
			double z = this.plugin.getYamlHandler().worlds.getDouble(worldName+".firstspawn.z");

			double dyaw = this.plugin.getYamlHandler().worlds.getDouble(worldName+".firstspawn.yaw");
			double dpitch = this.plugin.getYamlHandler().worlds.getDouble(worldName+".firstspawn.pitch");

			float yaw = (float)dyaw;
			float pitch = (float)dpitch;
						
			return new Location(world,x,y,z,yaw,pitch);

		}
		
		return world.getSpawnLocation();
	}

}

