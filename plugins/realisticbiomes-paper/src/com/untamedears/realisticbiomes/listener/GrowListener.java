package com.untamedears.realisticbiomes.listener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Biome;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Event listener for all plant growth related events. Whenever a crop, plant block, or sapling attempts to grow, its type
 * is checked against the biomes in which it is permitted to grow. If the biome is not permitted, the event is canceled and
 * the plant does not grow. Additionally, all instances of bonemeal being used as fertilizer are canceled.
 * @author WildWeazel
 *
 */
public class GrowListener implements Listener {

	/**
	 *  Maps a {@link Material} or {@link TreeType} to the {@link Biome}s in which it is permitted to grow
	 */
	private static Map<Object, Set<Biome>> allowedGrowth = new HashMap<Object, Set<Biome>>();

	static {
		allowedGrowth.put(	Material.CROPS,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.PLAINS,
				Biome.FOREST,
				Biome.FOREST_HILLS,
				Biome.MUSHROOM_ISLAND,
				Biome.RIVER,
				Biome.SKY,
				Biome.SMALL_MOUNTAINS,
				Biome.SWAMPLAND,
				Biome.TAIGA,
				Biome.TAIGA_HILLS,
				Biome.EXTREME_HILLS
			}))
		);
		allowedGrowth.put(	Material.MELON_STEM,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.PLAINS,
				Biome.FOREST,
				Biome.FOREST_HILLS,
				Biome.MUSHROOM_ISLAND,
				Biome.RIVER,
				Biome.SMALL_MOUNTAINS,
				Biome.SWAMPLAND,
				Biome.JUNGLE,
				Biome.JUNGLE_HILLS,
				Biome.SKY
			}))
		);
		allowedGrowth.put(	Material.MELON_BLOCK,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.PLAINS,
				Biome.FOREST,
				Biome.FOREST_HILLS,
				Biome.MUSHROOM_ISLAND,
				Biome.RIVER,
				Biome.SMALL_MOUNTAINS,
				Biome.SWAMPLAND,
				Biome.JUNGLE,
				Biome.JUNGLE_HILLS,
				Biome.SKY
			}))
		);
		allowedGrowth.put(	Material.PUMPKIN_STEM,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.PLAINS,
				Biome.FOREST,
				Biome.FOREST_HILLS,
				Biome.MUSHROOM_ISLAND,
				Biome.RIVER,
				Biome.SKY,
				Biome.SMALL_MOUNTAINS,
				Biome.SWAMPLAND
			}))
		);
		allowedGrowth.put(	Material.PUMPKIN,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.PLAINS,
				Biome.FOREST,
				Biome.FOREST_HILLS,
				Biome.MUSHROOM_ISLAND,
				Biome.RIVER,
				Biome.SKY,
				Biome.SMALL_MOUNTAINS,
				Biome.SWAMPLAND
			}))
		);
		allowedGrowth.put(	Material.CARROT,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.PLAINS,
				Biome.FOREST,
				Biome.FOREST_HILLS,
				Biome.MUSHROOM_ISLAND,
				Biome.RIVER
			}))
		);
		allowedGrowth.put(	Material.POTATO,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.PLAINS,
				Biome.FOREST,
				Biome.FOREST_HILLS,
				Biome.MUSHROOM_ISLAND,
				Biome.RIVER
			}))
		);
		allowedGrowth.put(	Material.COCOA,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.JUNGLE,
				Biome.JUNGLE_HILLS,
				Biome.MUSHROOM_ISLAND
			}))
		);
		allowedGrowth.put(	Material.CACTUS,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.DESERT,
				Biome.DESERT_HILLS,
				Biome.MUSHROOM_ISLAND
			}))
		);
		allowedGrowth.put(	Material.SUGAR_CANE_BLOCK,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.DESERT,
				Biome.DESERT_HILLS,
				Biome.SWAMPLAND,
				Biome.JUNGLE,
				Biome.JUNGLE_HILLS,
				Biome.BEACH,
				Biome.MUSHROOM_SHORE,
				Biome.RIVER
			}))
		);
		allowedGrowth.put(	Material.NETHER_STALK,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.HELL
			}))
		);
		allowedGrowth.put(	TreeType.TREE,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.FOREST,
				Biome.FOREST_HILLS,
				Biome.EXTREME_HILLS,
				Biome.JUNGLE,
				Biome.JUNGLE_HILLS,
				Biome.SMALL_MOUNTAINS,
				Biome.SWAMPLAND,
				Biome.ICE_MOUNTAINS,
				Biome.ICE_PLAINS,
				Biome.MUSHROOM_ISLAND
			}))
		);
		allowedGrowth.put(	TreeType.BIG_TREE,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.FOREST,
				Biome.FOREST_HILLS,
				Biome.MUSHROOM_ISLAND
			}))
		);
		allowedGrowth.put(	TreeType.BIRCH,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.FOREST,
				Biome.FOREST_HILLS,
				Biome.MUSHROOM_ISLAND
			}))
		);
		allowedGrowth.put(	TreeType.JUNGLE,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.JUNGLE,
				Biome.JUNGLE_HILLS,
				Biome.MUSHROOM_ISLAND
			}))
		);
		allowedGrowth.put(	TreeType.SMALL_JUNGLE,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.JUNGLE,
				Biome.JUNGLE_HILLS,
				Biome.MUSHROOM_ISLAND
			}))
		);
		allowedGrowth.put(	TreeType.JUNGLE_BUSH,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.JUNGLE,
				Biome.JUNGLE_HILLS,
				Biome.MUSHROOM_ISLAND
			}))
		);
		allowedGrowth.put(	TreeType.REDWOOD,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.TAIGA,
				Biome.TAIGA_HILLS,
				Biome.MUSHROOM_ISLAND,
				Biome.EXTREME_HILLS,
				Biome.ICE_MOUNTAINS,
				Biome.ICE_PLAINS
			}))
		);
		allowedGrowth.put(	TreeType.TALL_REDWOOD,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.TAIGA,
				Biome.TAIGA_HILLS,
				Biome.MUSHROOM_ISLAND
			}))
		);
		allowedGrowth.put(	TreeType.SWAMP,
			new HashSet<Biome>( Arrays.asList(new Biome[]{
				Biome.SWAMPLAND
			}))
		);
	}

	/**
	 *  Event handler for {@link BlockGrowEvent}. Checks plant growth for proper biomes.
	 * @param event The {@link BlockGrowEvent} being handled
	 */
	@EventHandler(ignoreCancelled = true)
	public void growBlock(BlockGrowEvent event) {
		Material m = event.getNewState().getType();
		Biome b = event.getBlock().getBiome();
		event.setCancelled(!canGrowHere(m, b));
	}

	/**
	 * Event handler for {@link StructureGrowEvent}. Checks tree growth for proper biomes.
	 * @param event The {@link StructureGrowEvent} being handled
	 */
	@EventHandler(ignoreCancelled = true)
	public void growStructure(StructureGrowEvent event) {
		Biome b = event.getLocation().getBlock().getBiome();
		TreeType t = event.getSpecies();
		event.setCancelled(!canGrowHere(t, b));
	}

	/**
	 * Event handler for {@link PlayerInteractEvent}. Cancels all uses of Bonemeal as an item.
	 * @param event The {@link PlayerInteractEvent} being handled
	 */
	@EventHandler(ignoreCancelled = true)
	
	public void onPlayerInteract(PlayerInteractEvent event) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        ItemStack item = event.getPlayer().getItemInHand();
                        // Ink Sack with data 15  == Bone Meal
                        if (item.getTypeId() == 351 && item.getData().getData() == 15) {
                                        event.setCancelled(true);
                        }
            }
        }
	

	/**
	 * Determines if a plant {@link Material} type can grow in a {@link Biome}
	 * @param m The material type of the plant
	 * @param b The biome in which the plant is growing
	 * @return Whether the plant type is allowed in the biome
	 */
	private boolean canGrowHere(Material m, Biome b) {
		if(allowedGrowth.containsKey(m)) {
			return allowedGrowth.get(m).contains(b);
		}
		return true;
	}

	/**
	 * Determines if a {@link TreeType} can grow in a {@link Biome}
	 * @param t The tree structure type
	 * @param b The biome in which the tree is growing
	 * @return Whether the tree type is allowed in the biome
	 */
	private boolean canGrowHere(TreeType t, Biome b) {
		if(allowedGrowth.containsKey(t)) {
			return allowedGrowth.get(t).contains(b);
		}
		return true;
	}

}
