package me.josvth.randomspawn.listeners;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;

import me.josvth.randomspawn.RandomSpawn;
import me.josvth.randomspawn.events.NewPlayerSpawn;

/**
 * Handle joins. 
 *
 */
public class JoinListener implements Listener{

	RandomSpawn plugin;

	public JoinListener(RandomSpawn instance) {
		plugin = instance;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event){ 
		plugin.sendGround(event.getPlayer(), event.getTo());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){

		Player player = event.getPlayer();
		String playerName = player.getName();

		World world = player.getWorld();
		String worldName = world.getName();

		if(world.getEnvironment().equals(Environment.NETHER) || world.getEnvironment().equals(Environment.THE_END)) return;

		if(player.hasPlayedBefore()) return;
		
		List<String> randomSpawnFlags = plugin.yamlHandler.worlds.getStringList(worldName + ".randomspawnon");
		List<String> spawnPointFlags = plugin.yamlHandler.worlds.getStringList(worldName + ".spawnpointson");
		
		if (!randomSpawnFlags.contains("firstjoin") && !spawnPointFlags.contains("firstjoin")){ 
			player.teleport(getFirstSpawn(world));
			plugin.logDebug(playerName + " is teleported to the first spawn of " + worldName);
			return; 
		}

		if (player.hasPermission("RandomSpawn.exclude")){ 																// checks if player should be excluded
			plugin.logDebug(playerName + " is excluded from Random Spawning.");
			return; 
		}
		
		if (spawnPointFlags.contains("firstjoin")) {
			plugin.logDebug(playerName + "First Join spawn point spawning");
			List<Location> spawnLocations = plugin.findSpawnPoints(world);
		
			int totalTries = spawnLocations.size();
			for (int i = 0 ; i < totalTries ; i++) {
				int j = (int) ( Math.random() * spawnLocations.size() );
				Location newSpawn = spawnLocations.get(j);
				NewPlayerSpawn nps = new NewPlayerSpawn(player, newSpawn );
				plugin.getServer().getPluginManager().callEvent(nps);
				if (nps.isCancelled()) {
					spawnLocations.remove(j);
				} else {
					plugin.sendGround(player, newSpawn);
					player.teleport(newSpawn.add(0, 3, 0));
					player.setMetadata("lasttimerandomspawned", new FixedMetadataValue(plugin, System.currentTimeMillis()));
					
					if (plugin.yamlHandler.worlds.getBoolean(worldName + ".keeprandomspawns",false)){
						player.setBedSpawnLocation(newSpawn);
					}
		
					if (plugin.yamlHandler.config.getString("messages.randomspawned") != null){
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.yamlHandler.config.getString("messages.randomspawned")));
					}
					return;
				}
			}
		}

		if (randomSpawnFlags.contains("firstjoin")) {
			plugin.logDebug(playerName + "First Join random spawning");
			Location spawnLocation = plugin.chooseSpawn(world);
		
			plugin.sendGround(player, spawnLocation);
			
			player.teleport(spawnLocation.add(0, 3, 0));
	
			player.setMetadata("lasttimerandomspawned", new FixedMetadataValue(plugin, System.currentTimeMillis()));
	
			if (plugin.yamlHandler.worlds.getBoolean(worldName + ".keeprandomspawns",false)){
				player.setBedSpawnLocation(spawnLocation);
			}
	
			if (plugin.yamlHandler.config.getString("messages.randomspawned") != null){
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.yamlHandler.config.getString("messages.randomspawned")));
			}
		}
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent event){
		if(event.getPlayer().hasMetadata("lasttimerandomspawned")){
			if((event.getPlayer().getMetadata("lasttimerandomspawned").get(0).asLong() + (plugin.yamlHandler.config.getInt("nodamagetime",5)*1000)) > System.currentTimeMillis()){
				event.setReason("");
				event.setLeaveMessage("");
				event.setCancelled(true);
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
