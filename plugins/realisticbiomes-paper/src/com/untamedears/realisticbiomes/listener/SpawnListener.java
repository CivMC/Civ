package com.untamedears.realisticbiomes.listener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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

	static {
		allowedGrowth.put(	EntityType.CHICKEN,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.FOREST,
				Biome.FOREST_HILLS,
				Biome.JUNGLE,
				Biome.JUNGLE_HILLS,
				Biome.MUSHROOM_ISLAND,
				Biome.MUSHROOM_SHORE,
				Biome.PLAINS,
				Biome.RIVER,
				Biome.SKY,
				Biome.SMALL_MOUNTAINS,
				Biome.SWAMPLAND
			}))
		);
		allowedGrowth.put(	EntityType.COW,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.FOREST,
				Biome.FOREST_HILLS,
				Biome.MUSHROOM_ISLAND,
				Biome.MUSHROOM_SHORE,
				Biome.PLAINS,
				Biome.RIVER,
				Biome.SMALL_MOUNTAINS,
				Biome.TAIGA,
				Biome.TAIGA_HILLS
			}))
		);
		allowedGrowth.put(	EntityType.MUSHROOM_COW,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.MUSHROOM_ISLAND,
				Biome.MUSHROOM_SHORE
			}))
		);
		allowedGrowth.put(	EntityType.PIG,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.FOREST,
				Biome.FOREST_HILLS,
				Biome.JUNGLE,
				Biome.JUNGLE_HILLS,
				Biome.MUSHROOM_ISLAND,
				Biome.MUSHROOM_SHORE,
				Biome.PLAINS,
				Biome.RIVER,
				Biome.SMALL_MOUNTAINS,
				Biome.SWAMPLAND
			}))
		);
		allowedGrowth.put(	EntityType.SHEEP,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.FOREST,
				Biome.FOREST_HILLS,
				Biome.MUSHROOM_ISLAND,
				Biome.MUSHROOM_SHORE,
				Biome.PLAINS,
				Biome.RIVER,
				Biome.SMALL_MOUNTAINS,
				Biome.TAIGA,
				Biome.TAIGA_HILLS,
				Biome.EXTREME_HILLS
			}))
		);
	}

	@EventHandler(ignoreCancelled = true)
	public void spawnEntity(CreatureSpawnEvent event) {
		if(event.getSpawnReason() == SpawnReason.BREEDING) {
			EntityType type = event.getEntityType();
			Block block = event.getLocation().getBlock();
			event.setCancelled(!canSpawnHere(type, block));
		}
	}

	private boolean canSpawnHere(EntityType m, Block b) {
		if(allowedGrowth.containsKey(m)) {
			return allowedGrowth.get(m).contains(b.getBiome());
		}
		return true;
	}

}
