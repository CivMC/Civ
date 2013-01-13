package com.untamedears.realisticbiomes.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class SpawnListener implements Listener {

	private static Map<EntityType, Set<Biome>> allowedGrowth = new HashMap<EntityType, Set<Biome>>();

	@EventHandler(ignoreCancelled = true)
	public void spawnEntity(CreatureSpawnEvent event) {
		if(event.getSpawnReason() == SpawnReason.BREEDING) {
			EntityType type = event.getEntityType();
			Block block = event.getLocation().getBlock();
			event.setCancelled(canSpawnHere(type, block));
		}
	}

	private boolean canSpawnHere(EntityType m, Block b) {
		if(allowedGrowth.containsKey(m)) {
			return allowedGrowth.get(m).contains(b.getBiome());
		}
		return true;
	}

}
