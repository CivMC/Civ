package com.untamedears.realisticbiomes.persist;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.material.CocoaPlant;
import org.bukkit.material.CocoaPlant.CocoaPlantSize;
import org.bukkit.material.MaterialData;

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
		
		if (growth > 1.0f) {
			growth = 1.0f;
		}
		
		byte stage = (byte)(((float)(stages-1))*growth);
		BlockState state = block.getState();
		MaterialData data = state.getData();
		if (data instanceof CocoaPlant) {
			// trust that enum order is sanely declared in order SMALL, MEDIUM, LARGE
			CocoaPlantSize cocoaSize = CocoaPlantSize.values()[stage]; 
			((CocoaPlant)data).setSize(cocoaSize);
		} else {
			data.setData(stage);
		}
		state.setData(data);
		state.update(true, false);
	}
	
	public static double getGrowthFraction(Block block) {
		if (!growthStages.containsKey(block.getType()))
			return 0.0;
		
		byte stage;
		MaterialData data = block.getState().getData();
		if (data instanceof CocoaPlant) {
			CocoaPlantSize cocoaSize = ((CocoaPlant) data).getSize();
			switch (cocoaSize) {
				case SMALL:
					stage = 0;
					break;
				case MEDIUM:
					stage = 1;
					break;
				case LARGE:
				default:
					stage = 2;
					break;
			}
		} else {
			stage = data.getData();
		}
		return (double)stage/(double)(growthStages.get(block.getType())-1);
	}
}
