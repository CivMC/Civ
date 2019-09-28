package com.untamedears.realisticbiomes.growth;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;

public abstract class IArtificialGrower {

	public static IArtificialGrower getAppropriateGrower(Material material) {
		switch (material) {
		case CARROT:
			return new AgeableGrower(7, 3);
		case POTATO:
		case WHEAT:
			return new AgeableGrower(8, 1);
		case COCOA:
			return new AgeableGrower(3, 1);
		case ACACIA_SAPLING:
		case OAK_SAPLING:
		case BIRCH_SAPLING:
			return new TreeGrower();
		default:
			throw new IllegalArgumentException(material.name() + " can not be grown");
		}
	}

	public void fullyGrow(Block block) {
		setStage(block, getMaxStage());
	}
	
	public abstract int getMaxStage();

	public double getProgressGrowthStage(Block block) {
		return (double) getStage(block) / getMaxStage();
	}
	
	public abstract int getStage(Block block);
	
	public abstract int getIncrementPerStage();

	public abstract void setPersistentProgress(PlantGrowthConfig config, Block block, double progress);

	public abstract void setStage(Block block, int stage);

}
