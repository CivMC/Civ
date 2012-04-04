package me.Josvth.RandomSpawn.Listeners;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.Josvth.RandomSpawn.RandomSpawn;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class DebugListener implements Listener {
	private RandomSpawn plugin;

	public DebugListener(RandomSpawn instance) {
		plugin = instance;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onPlayerDeath(EntityDeathEvent event){
		if(event.getEntity() instanceof Player){
			
			Player player = (Player)event.getEntity();
			
			if(player.hasMetadata("lasttimerandomspawned") && player.getMetadata("lasttimerandomspawned").get(0).asLong() + 5000 > System.currentTimeMillis()){		// Player is likely to be killed during random spawning
				
				File debugFile = new File(plugin.getDataFolder(), player.getName() + " " + new SimpleDateFormat("dd-MM-yyyy,HH:mm:ss").format(new Date()) + ".yml");
				FileConfiguration debugYaml = new YamlConfiguration();
				
				debugYaml.set("name", player.getName());
				debugYaml.set("cause", player.getLastDamageCause().getCause().name());
				debugYaml.set("falldistance", player.getFallDistance());
				debugYaml.set("velocity", player.getVelocity());
				debugYaml.set("maxnodamagetickes", player.getMaximumNoDamageTicks());
				debugYaml.set("nodamagetickes", player.getNoDamageTicks());
				debugYaml.set("location", player.getLocation());
				debugYaml.set("timeafterspawning", System.currentTimeMillis() - player.getMetadata("lasttimerandomspawned").get(0).asLong());
				
				try {
					debugYaml.save(debugFile);
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
				
				plugin.logWarning("Player " + player.getName() + " died likely because of randomspawning. Please send the following file to josvth@live.nl: " + debugFile.getPath());
				
			}
			
		}
	}
}
