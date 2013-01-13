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
		allowedGrowth.put(Material.CROPS, new HashSet<Biome>( Arrays.asList(new Biome[]{ Biome.PLAINS }) ));
		allowedGrowth.put(Material.MELON_STEM, new HashSet<Biome>( Arrays.asList(new Biome[]{ Biome.PLAINS }) ));
		allowedGrowth.put(Material.PUMPKIN_STEM, new HashSet<Biome>( Arrays.asList(new Biome[]{ Biome.PLAINS }) ));
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
