package com.untamedears.realisticbiomes;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.untamedears.realisticbiomes.growth.ColumnPlantGrower;
import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;
import com.untamedears.realisticbiomes.model.Plant;
import com.untamedears.realisticbiomes.utils.RBUtils;

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
		PlantGrowthConfig growthConfig = growthConfigManager.getPlantGrowthConfig(block);
		if (growthConfig == null || !growthConfig.isPersistent()) {
			return;
		}
		Plant plant = plantManager.getPlant(block);
		if (plant == null) {
			//column plants will always hold the plant object in the bottom most block, so we need 
			//to update that if we just broke one of the upper blocks of a column plant
			if (!RBUtils.isColumnPlant(block.getType())) {
				return;
			}
			Block sourceColumn = ColumnPlantGrower.getRelativeBlock(block, BlockFace.DOWN);
			Plant bottomColumnPlant = plantManager.getPlant(sourceColumn);
			if (bottomColumnPlant != null) {
				bottomColumnPlant.resetCreationTime();
			}
			//TODO This slows down growth if sugar cane is broken when it's 2 tall, because it fully resets
			// the timer, when waiting a shorter period of time would grow the third level
			return;
		}
		plantManager.deletePlant(plant);
	}

	public void handlePlantCreation(Block block) {
		if (plantManager == null) {
			return;
		}
		PlantGrowthConfig growthConfig = growthConfigManager.getPlantGrowthConfig(block);
		if (growthConfig == null || !growthConfig.isPersistent()) {
			return;
		}
		Plant plant = new Plant(block.getLocation());
		plantManager.putPlant(plant);
		initGrowthTime(plant);
	}

	public void initGrowthTime(Plant plant) {
		Block block = plant.getLocation().getBlock();
		PlantGrowthConfig growthConfig = growthConfigManager.getPlantGrowthConfig(block);
		if (growthConfig == null || !growthConfig.isPersistent()) {
			return;
		}
		long nextUpdateTime = growthConfig.updatePlant(plant);
		plant.setNextGrowthTime(nextUpdateTime);
	}

}
