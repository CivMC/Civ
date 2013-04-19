package me.josvth.randomspawn.listeners;

import me.josvth.randomspawn.RandomSpawn;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class DamageListener implements Listener{
	
	RandomSpawn plugin;
	
	public DamageListener(RandomSpawn instance){
		plugin = instance;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent event){
		if(event.getEntity() instanceof Player && event.getEntity().hasMetadata("lasttimerandomspawned") && !event.getCause().equals(DamageCause.SUICIDE)){
			if((event.getEntity().getMetadata("lasttimerandomspawned").get(0).asLong() + (plugin.yamlHandler.config.getInt("nodamagetime",5)*1000)) > System.currentTimeMillis()){
				event.setCancelled(true);
			}
		}
	}
}
