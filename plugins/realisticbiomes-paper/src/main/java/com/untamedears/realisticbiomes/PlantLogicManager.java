package com.untamedears.realisticbiomes;

import org.bukkit.Chunk;
import org.bukkit.block.Block;

import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;
import com.untamedears.realisticbiomes.model.GlobalPlantManager;
import com.untamedears.realisticbiomes.model.Plant;

public class PlantLogicManager {
	
	private GlobalPlantManager plantManager;
	private GrowthConfigManager growthConfigManager;
	
	public PlantLogicManager(GlobalPlantManager plantManager, GrowthConfigManager growthConfigManager) {
		this.plantManager = plantManager;
		this.growthConfigManager = growthConfigManager;
	}
	
	public void handleBlockDestruction(Block block) {
		if (plantManager == null) {
			return;
		}
		PlantGrowthConfig growthConfig = growthConfigManager.getPlantGrowthConfig(block.getType());
		if (growthConfig == null || !growthConfig.isPersistent()) {
			return;
		}
		Plant plant = plantManager.getPlant(block);
		if (plant == null) {
			return;
		}
		plantManager.removePlant(plant);
	}
	
	public void handleChunkLoad(Chunk chunk) {
		if (plantManager != null) {
			plantManager.loadChunkData(chunk);
		}
	}
	
	public void handleChunkUnload(Chunk chunk) {
		if (plantManager != null) {
			plantManager.unloadChunkData(chunk);
		}
	}
	
	public void handlePlantCreation(Block block) {
		if (plantManager == null) {
			return;
		}
		PlantGrowthConfig growthConfig = growthConfigManager.getPlantGrowthConfig(block.getType());
		if (growthConfig == null || !growthConfig.isPersistent()) {
			return;
		}
		Plant plant = new Plant(block.getLocation());
		plant.innerUpdateGrowthTime(growthConfig.updatePlant(plant, block));
		plantManager.insertPlant(plant);
	}

}
