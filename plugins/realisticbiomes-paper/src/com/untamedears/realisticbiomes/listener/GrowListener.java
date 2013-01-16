package com.untamedears.realisticbiomes.listener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;

public class GrowListener implements Listener {

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

	@EventHandler(ignoreCancelled = true)
	public void growBlock(BlockGrowEvent event) {
		Material m = event.getNewState().getType();
		Biome b = event.getBlock().getBiome();
		event.setCancelled(!canGrowHere(m, b));
	}

	@EventHandler(ignoreCancelled = true)
	public void growStructure(StructureGrowEvent event) {
		Biome b = event.getLocation().getBlock().getBiome();
		TreeType t = event.getSpecies();
		event.setCancelled(!canGrowHere(t, b));
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
	    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			ItemStack item = event.getItem();
			// Ink Sack with data 15  == Bone Meal
			if (item.getType() == Material.INK_SACK	&& item.getData().getData() == 15) {
			    if (event.hasBlock()) {
			    	Block b = event.getClickedBlock();
			    	event.setCancelled(!canGrowHere(b.getType(), b.getBiome()));
			    }
			}
	    }
	}

	private boolean canGrowHere(Material m, Biome b) {
		if(allowedGrowth.containsKey(m)) {
			return allowedGrowth.get(m).contains(b);
		}
		return true;
	}

	private boolean canGrowHere(TreeType t, Biome b) {
		if(allowedGrowth.containsKey(t)) {
			return allowedGrowth.get(t).contains(b);
		}
		return true;
	}

}
