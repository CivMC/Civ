package com.untamedears.realisticbiomes.listener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerFishEvent;

/**
 * Event listeners for animal spawn related events. Whenever animals breed or a fish is caught, the species is checked against
 * allowed biomes. If the biome is not found, the event is cancelled and nothing happens. Also, chicken egg spawns are cancelled
 * randomly to reduce the average time between laying eggs.
 * @author WildWeazel
 *
 */
public class SpawnListener implements Listener {

	/**
	 *  Maps a {@link EntityType} to the {@link Biome}s in which it is permitted to spawn
	 */
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
		allowedGrowth.put(	EntityType.FISHING_HOOK,
				new HashSet<Biome>( Arrays.asList(new Biome[]{
					Biome.RIVER,
					Biome.OCEAN,
					Biome.BEACH,
					Biome.FROZEN_RIVER,
					Biome.FROZEN_OCEAN
				}))
			);
	}

	/**
	 *  Event handler for {@link CreatureSpawnEvent}. Checks animal breeding for proper biomes.
	 * @param event The {@link CreatureSpawnEvent} being handled
	 */
	@EventHandler(ignoreCancelled = true)
	public void spawnEntity(CreatureSpawnEvent event) {
		if(event.getSpawnReason() == SpawnReason.BREEDING) {
			EntityType type = event.getEntityType();
			Block block = event.getLocation().getBlock();
			event.setCancelled(!canSpawnHere(type, block));
		}
	}

	public static Logger LOG = Logger.getLogger("RealisticBiomes");
	/**
	 *  Event handler for {@link ItemSpawnEvent}. Reduces the chance of a chicken egg being spawned.
	 * @param event The {@link ItemSpawnEvent} being handled
	 */
	@EventHandler(ignoreCancelled = true)
	public void spawnItem(ItemSpawnEvent event) {
		if(event.getEntity().getItemStack().getType() == Material.EGG) {
			event.setCancelled(Math.random() > 0.1);
		}
	}

	/**
	 *  Event handler for {@link PlayerFishEvent}. Checks caught fish for proper biomes.
	 * @param event The {@link PlayerFishEvent} being handled
	 */
	@EventHandler(ignoreCancelled = true)
	public void fishing(PlayerFishEvent event) {
		if( event.getState() == PlayerFishEvent.State.CAUGHT_FISH && event.getCaught() != null ) {
			EntityType type = EntityType.FISHING_HOOK;
			Block block = event.getCaught().getLocation().getBlock();
			event.setCancelled(!canSpawnHere(type, block));
		}
	}

	/**
	 * Determines if a plant {@link EntityType} type can spawn in a {@link Biome}
	 * @param t The entity type being spawned
	 * @param b The biome in which the spawn occurs
	 * @return Whether the entity is allowed to spawn in the biome
	 */
	private boolean canSpawnHere(EntityType t, Block b) {
		if(allowedGrowth.containsKey(t)) {
			return allowedGrowth.get(t).contains(b.getBiome());
		}
		return true;
	}

}
