package me.Josvth.RandomSpawn.Listeners;

import java.io.File;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.Josvth.RandomSpawn.RandomSpawn;

public class JoinListener implements Listener{

	RandomSpawn plugin;

	public JoinListener(RandomSpawn instance) {
		plugin = instance;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){

		Player player = event.getPlayer();
		String playerName = player.getName();

		World world = player.getWorld();
		String worldName = world.getName();

		if(world.getEnvironment().equals(Environment.NETHER) || world.getEnvironment().equals(Environment.THE_END)) return;
				
		File file = new File(world.getWorldFolder() + File.separator + "players" + File.separator + player.getName() + ".dat");

		if(file.exists()) return;

		List<String> randomSpawnFlags = plugin.yamlHandler.worlds.getStringList(worldName + ".randomspawnon");

		if (!randomSpawnFlags.contains("firstjoin")){ 
			player.teleport(getFirstSpawn(world));
			plugin.logDebug(playerName + " is teleported to the first spawn of " + worldName);
			return; 
		}

		if (player.hasPermission("RandomSpawn.exclude")){ 																// checks if player should be excluded
			plugin.logDebug(playerName + " is excluded from Random Spawning.");
			return; 
		}

		if (randomSpawnFlags.contains("respawn")){

			Location spawnLocation = plugin.chooseSpawn(world);

			plugin.sendGround(player, spawnLocation);

			player.teleport(spawnLocation);
						
			player.setNoDamageTicks(plugin.yamlHandler.config.getInt("nodamagetime",5)*20);
			
			if (plugin.yamlHandler.worlds.getBoolean(worldName + ".keeprandomspawns",false)){
				player.setBedSpawnLocation(spawnLocation);
			}
				
			if (plugin.yamlHandler.config.getString("messages.randomspawned") != null){
				player.sendMessage(plugin.yamlHandler.config.getString("messages.randomspawned"));
			}
			
		}			
	}
	
	private Location getFirstSpawn(World world) {
		String worldName = world.getName();

		if (plugin.yamlHandler.worlds.contains(worldName +".firstspawn")){

			double x = plugin.yamlHandler.worlds.getDouble(worldName+".firstspawn.x");
			double y = plugin.yamlHandler.worlds.getDouble(worldName+".firstspawn.y");
			double z = plugin.yamlHandler.worlds.getDouble(worldName+".firstspawn.z");

			double dyaw = plugin.yamlHandler.worlds.getDouble(worldName+".firstspawn.yaw");
			double dpitch = plugin.yamlHandler.worlds.getDouble(worldName+".firstspawn.pitch");

			float yaw = (float)dyaw;
			float pitch = (float)dpitch;

			return new Location(world,x,y,z,yaw,pitch);

		}

		return world.getSpawnLocation();
	}

}
