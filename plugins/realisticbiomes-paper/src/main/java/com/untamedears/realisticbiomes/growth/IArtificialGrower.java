package com.untamedears.realisticbiomes.growth;

import org.bukkit.Material;
import org.bukkit.block.Block;

public abstract class IArtificialGrower {

	public static IArtificialGrower getAppropriateGrower(Material material) {
		switch (material) {
		case POTATO:
		case CARROT:
			return new AgeableGrower(7, 3);
		case WHEAT:
		case WHEAT_SEEDS:
		case MELON_SEEDS:
		case PUMPKIN_SEEDS:
			return new AgeableGrower(7, 1);
		case COCOA:
			return new AgeableGrower(2, 1);
		case OAK_SAPLING:
		case BIRCH_SAPLING:
		case ACACIA_SAPLING:
		case CHORUS_FLOWER:
		case DARK_OAK_SAPLING:
		case JUNGLE_SAPLING:
		case SPRUCE_SAPLING:
			return new TreeGrower();
		case NETHER_WART:
		case BEETROOT_SEEDS:
		case BEETROOTS:
			return new AgeableGrower(3, 1);
		case PUMPKIN:
		case MELON:
			return new FruitGrower();
		case CACTUS:
		case SUGAR_CANE:
			return new ColumnPlantGrower(3);
		case BAMBOO:
			return new BambooGrower(12);
		default:
			throw new IllegalArgumentException(material.name() + " can not be grown");
		}
	}

	public void fullyGrow(Block block) {
		setStage(block, getMaxStage());
	}

	public abstract int getIncrementPerStage();

	public abstract int getMaxStage();

	public double getProgressGrowthStage(Block block) {
		return (double) getStage(block) / getMaxStage();
	}

	public abstract int getStage(Block block);

	public abstract void setStage(Block block, int stage);

}
