package com.untamedears.realisticbiomes.growth;

import org.bukkit.block.Block;

public abstract class IArtificialGrower {

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
