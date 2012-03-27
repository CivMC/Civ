package me.Josvth.RandomSpawn.Listeners;

import me.Josvth.RandomSpawn.RandomSpawn;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class RandomSpawnDebugListener implements Listener {
	private RandomSpawn plugin;

	public RandomSpawnDebugListener(RandomSpawn instance) {
		plugin = instance;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onPlayerDeath(EntityDeathEvent event){
		if(event.getEntity() instanceof Player){
			Player player = (Player)event.getEntity();
			plugin.logDebug(player.getName() + " died from " + player.getLastDamageCause().getCause().name());
			plugin.logDebug(player.getName() + " Falldistance: " + player.getFallDistance() + " Velocity: " + player.getVelocity() + " No damage tickes: " + player.getNoDamageTicks() + " Location: " + player.getLocation());
		}
	}
}
