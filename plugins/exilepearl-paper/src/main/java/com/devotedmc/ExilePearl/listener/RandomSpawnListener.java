package com.devotedmc.ExilePearl.listener;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;

import me.josvth.randomspawn.events.NewPlayerSpawn;

public class RandomSpawnListener extends RuleListener {

	
	public RandomSpawnListener(ExilePearlApi pearlApi) {
		super(pearlApi);
	}


	/**
	 * Prevents exiled players from random-spawning within their pearl radius
	 * @param e The event args
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onRandomSpawn(NewPlayerSpawn e) {
		int radius = config.getRulePearlRadius();
		if (radius <= 0) {
			return;
		}
		
		ExilePearl pearl = pearlApi.getPearl(e.getPlayer().getUniqueId());
		if (pearl == null) {
			return;
		}
		
		Location pearlLocation = pearl.getLocation();
		Location playerLocation = e.getLocation();
		
		if (pearlLocation.getWorld() != playerLocation.getWorld()) {
			return;
		}
		
		double distance = Math.sqrt(Math.pow(pearlLocation.getX() - playerLocation.getX(), 2) + Math.pow(pearlLocation.getZ() - playerLocation.getZ(), 2));
		
		if (distance < radius) {
			e.setCancelled(true);
		}
	}
}
