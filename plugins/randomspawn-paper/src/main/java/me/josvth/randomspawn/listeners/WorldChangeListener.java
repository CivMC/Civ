package me.josvth.randomspawn.listeners;

import java.util.List;
import me.josvth.randomspawn.RandomSpawn;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class WorldChangeListener implements Listener {

	RandomSpawn plugin;

	public WorldChangeListener(RandomSpawn instance){
		plugin = instance;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onPlayerWorldChange(PlayerChangedWorldEvent event){

		final Player player = event.getPlayer();
		String playerName = player.getName();

		if (player.hasPermission("RandomSpawn.exclude")){                                 // checks if player should be excluded
			plugin.logDebug(playerName + " is excluded from Random Spawning.");
			return;
		}

		World from = event.getFrom();
		World to = player.getWorld();

		if(player.getBedSpawnLocation() != null && to.equals(player.getBedSpawnLocation().getWorld())) return;      // players bed is in this world

		List<String> randomSpawnFlags = plugin.yamlHandler.worlds.getStringList(to.getName() + ".randomspawnon");

		if(randomSpawnFlags.contains("teleport-from-" + from.getName())){

			Location spawnLocation = plugin.getSpawnSelector().getRandomSpawnLocation(to);

			if (spawnLocation == null) {
				plugin.logDebug(playerName + " got unlucky and was not successfully randomspawned. Default behavior will apply");
				return;
			}

			plugin.sendGround(player, spawnLocation);

			player.teleport(spawnLocation.add(0, 5, 0));

			player.setMetadata("lasttimerandomspawned", new FixedMetadataValue(plugin, System.currentTimeMillis()));

			if (plugin.yamlHandler.worlds.getBoolean(to.getName() + ".keeprandomspawns",false)){
				player.setBedSpawnLocation(spawnLocation);
			}

			if (plugin.yamlHandler.config.getString("messages.randomspawned") != null){
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.yamlHandler.config.getString("messages.randomspawned")));
			}
		}
	}
}
