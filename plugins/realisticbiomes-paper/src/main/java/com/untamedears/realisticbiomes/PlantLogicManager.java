package com.untamedears.realisticbiomes;

import org.bukkit.block.Block;

import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;
import com.untamedears.realisticbiomes.model.Plant;

public class PlantLogicManager {

	private PlantManager plantManager;
	private GrowthConfigManager growthConfigManager;

	public PlantLogicManager(PlantManager plantManager, GrowthConfigManager growthConfigManager) {
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
		plantManager.deletePlant(plant);
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
		plantManager.putPlant(plant);
		initGrowthTime(plant);
	}

	public void initGrowthTime(Plant plant) {
		Block block = plant.getLocation().getBlock();
		PlantGrowthConfig growthConfig = growthConfigManager.getPlantGrowthConfig(block.getType());
		if (growthConfig == null || !growthConfig.isPersistent()) {
			return;
		}
		plant.setNextGrowthTime(growthConfig.updatePlant(plant));
	}

}
