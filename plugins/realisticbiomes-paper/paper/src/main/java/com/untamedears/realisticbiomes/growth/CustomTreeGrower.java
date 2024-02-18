package com.untamedears.realisticbiomes.growth;

import com.untamedears.realisticbiomes.model.Plant;
import org.bukkit.block.Block;

public class CustomTreeGrower extends IArtificialGrower {

	@Override
	public int getIncrementPerStage() {
		return 1;
	}

	@Override
	public int getMaxStage() {
		return 1;
	}

	@Override
	public int getStage(Plant plant) {
		Block block = plant.getLocation().getBlock();
		if (block.getType() != plant.getGrowthConfig().getItem().getType()) {
			return 1;
		}
		return 0;
	}

	@Override
	public boolean setStage(Plant plant, int stage) {
		if (stage < 1) {
			return true;
		}

		return true;
	}

	@Override
	public boolean deleteOnFullGrowth() {
		return true;
	}

}
