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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

public class GrowListener implements Listener {

	private static final Logger LOG = Logger.getLogger("RealisticBiomes");

	private static Map<Material, Set<Biome>> allowedGrowth = new HashMap<Material, Set<Biome>>();

	static {
		allowedGrowth.put(Material.CROPS, new HashSet<Biome>( Arrays.asList(new Biome[]{ Biome.PLAINS, Biome.FOREST, Biome.FOREST_HILLS, Biome.MUSHROOM_ISLAND, Biome.RIVER, Biome.SKY, Biome.SMALL_MOUNTAINS, Biome.SWAMPLAND, Biome.TAIGA, Biome.TAIGA_HILLS, Biome.EXTREME_HILLS }) ));
		allowedGrowth.put(Material.MELON_STEM, new HashSet<Biome>( Arrays.asList(new Biome[]{ Biome.PLAINS, Biome.FOREST, Biome.FOREST_HILLS, Biome.MUSHROOM_ISLAND, Biome.RIVER, Biome.SMALL_MOUNTAINS, Biome.SWAMPLAND, Biome.JUNGLE, Biome.JUNGLE_HILLS }) ));
		allowedGrowth.put(Material.PUMPKIN_STEM, new HashSet<Biome>( Arrays.asList(new Biome[]{ Biome.PLAINS, Biome.FOREST, Biome.FOREST_HILLS, Biome.MUSHROOM_ISLAND, Biome.RIVER, Biome.SKY, Biome.SMALL_MOUNTAINS, Biome.SWAMPLAND }) ));
		allowedGrowth.put(Material.CARROT, new HashSet<Biome>( Arrays.asList(new Biome[]{ Biome.PLAINS, Biome.FOREST, Biome.FOREST_HILLS, Biome.MUSHROOM_ISLAND, Biome.RIVER }) ));
		allowedGrowth.put(Material.POTATO, new HashSet<Biome>( Arrays.asList(new Biome[]{ Biome.PLAINS, Biome.FOREST, Biome.FOREST_HILLS, Biome.MUSHROOM_ISLAND, Biome.RIVER }) ));
		allowedGrowth.put(Material.COCOA, new HashSet<Biome>( Arrays.asList(new Biome[]{ Biome.JUNGLE, Biome.JUNGLE_HILLS, Biome.MUSHROOM_ISLAND }) ));
		allowedGrowth.put(Material.CACTUS, new HashSet<Biome>( Arrays.asList(new Biome[]{ Biome.DESERT, Biome.DESERT_HILLS, Biome.MUSHROOM_ISLAND }) ));
		allowedGrowth.put(Material.SUGAR_CANE_BLOCK, new HashSet<Biome>( Arrays.asList(new Biome[]{ Biome.DESERT, Biome.DESERT_HILLS, Biome.SWAMPLAND, Biome.JUNGLE, Biome.JUNGLE_HILLS, Biome.BEACH, Biome.MUSHROOM_SHORE, Biome.RIVER }) ));
		allowedGrowth.put(Material.SAPLING, new HashSet<Biome>( Arrays.asList(new Biome[]{ Biome.FOREST, Biome.FOREST_HILLS, Biome.MUSHROOM_ISLAND, Biome.SMALL_MOUNTAINS, Biome.SWAMPLAND, Biome.TAIGA, Biome.TAIGA_HILLS, Biome.EXTREME_HILLS, Biome.JUNGLE, Biome.JUNGLE_HILLS }) ));
		allowedGrowth.put(Material.NETHER_STALK, new HashSet<Biome>( Arrays.asList(new Biome[]{ Biome.HELL }) ));
	}

	@EventHandler(ignoreCancelled = true)
	public void growBlock(BlockGrowEvent event) {
		Material blockType = event.getBlock().getType();
		Block block = event.getBlock();
		event.setCancelled(!canGrowHere(blockType, block));
	}

	private boolean canGrowHere(Material m, Block b) {
		if(allowedGrowth.containsKey(m)) {
			return allowedGrowth.get(m).contains(b.getBiome());
		}
		return true;
	}

}
