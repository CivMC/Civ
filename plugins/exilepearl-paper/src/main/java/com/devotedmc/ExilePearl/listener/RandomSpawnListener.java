package com.devotedmc.ExilePearl.listener;

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
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onRandomSpawn(NewPlayerSpawn e) {
		int radius = config.getRulePearlRadius();
		if (radius <= 0) {
			return;
		}
		
		ExilePearl pearl = pearlApi.getPearl(e.getPlayer().getUniqueId());
		if (pearl == null) {
			return;
		}
		
		if (pearl.getLocation().getWorld() != e.getLocation().getWorld()) {
			return;
		}
		
		if (pearl.getLocation().distance(e.getLocation()) < radius) {
			e.setCancelled(true);
		}
	}
}
