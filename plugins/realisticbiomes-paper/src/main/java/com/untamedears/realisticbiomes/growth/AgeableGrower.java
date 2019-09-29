package com.untamedears.realisticbiomes.growth;

import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;

/**
 * Handles growing of anything implementing Ageable (Crops, Cocoa etc.)
 */
public class AgeableGrower extends IArtificialGrower {

	private int maxStage;
	private int increment;

	public AgeableGrower(int maxStage, int increment) {
		this.maxStage = maxStage;
		this.increment = increment;
	}

	@Override
	public int getIncrementPerStage() {
		return increment;
	}

	@Override
	public int getMaxStage() {
		return maxStage;
	}

	@Override
	public int getStage(Block block) {
		if (!(block.getBlockData() instanceof Ageable)) {
			return -1;
		}
		return ((Ageable) block.getBlockData()).getAge();
	}

	@Override
	public void setStage(Block block, int stage) {
		if (!(block.getBlockData() instanceof Ageable)) {
			throw new IllegalArgumentException("Can not set age for non Ageable");
		}
		Ageable ageable = ((Ageable) block.getBlockData());
		ageable.setAge(stage);
		block.setBlockData(ageable, true);
	}

}
