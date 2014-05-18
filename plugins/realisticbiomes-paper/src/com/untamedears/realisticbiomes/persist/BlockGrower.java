package com.untamedears.realisticbiomes.persist;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.block.Block;

// handles force-growing of crop-type blocks based on a fractional growth amount
public class BlockGrower {
	
	public static Logger LOG = Logger.getLogger("RealisticBiomes");
	
	PlantManager plantManager;
	
	// store the total growth stages of plants
	public static HashMap<Material, Integer> growthStages = new HashMap<Material, Integer>();
	static {
		growthStages.put(Material.CROPS, 8);
		growthStages.put(Material.CARROT, 8);
		growthStages.put(Material.POTATO, 8);
		
		growthStages.put(Material.MELON_STEM, 8);
		growthStages.put(Material.PUMPKIN_STEM, 8);
		
		growthStages.put(Material.COCOA, 3);
		
		growthStages.put(Material.NETHER_WARTS, 4);
		
		growthStages.put(Material.SAPLING, 1);
	}
	
	public BlockGrower(PlantManager plantManager) {
		this.plantManager = plantManager;
	}
	
	// grow the crop found at the given block/coordinates, with the amount or growth
	// between 0 and 1, with 1 being totally mature
	public void growBlock(Block block, float growth) {
		Integer stages = growthStages.get(block.getType());
		if (stages == null)
			return;
		
		byte stage = (byte)(((float)(stages-1))*growth);
		if (block.getType() == Material.COCOA) {
			stage = (byte)((block.getData()%4) + stage*4);
		}
		block.setData(stage);
		
		// if the plant is finished growing, then remove it from the manager
		if (growth >= 1.0) {
			stage = (byte)(stages - 1);
			if (block.getType() == Material.COCOA)
				stage = (byte)((block.getData()%4) + stage*4);
			
			block.setData((byte) stage);
		}
	}
	
	public static double getGrowthFraction(Block block) {
		if (!growthStages.containsKey(block.getType()))
			return 0.0;
		
		byte stage = block.getData();
		if (block.getType() == Material.COCOA) {
			stage = (byte)(block.getData()/4);
		}
		return (double)stage/(double)(growthStages.get(block.getType())-1);
	}
}
