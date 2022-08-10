package com.untamedears.realisticbiomes.growth;

import com.untamedears.realisticbiomes.model.Plant;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;

/**
 * Handles growing of anything implementing Ageable (Crops, Cocoa etc.)
 */
public class AgeableGrower extends IArtificialGrower {

	protected final int maxStage;
	protected final int increment;
	protected final Material material;

	public AgeableGrower(Material material, int maxStage, int increment) {
		this.maxStage = maxStage;
		this.increment = increment;
		this.material = material;
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
	public int getStage(Plant plant) {
		Block block = plant.getLocation().getBlock();
		if (block.getType() != material) {
			return -1;
		}
		if (!(block.getBlockData() instanceof Ageable)) {
			return -1;
		}
		return ((Ageable) block.getBlockData()).getAge();
	}

	@Override
	public boolean setStage(Plant plant, int stage) {
		Block block = plant.getLocation().getBlock();
		if (!(block.getBlockData() instanceof Ageable)) {
			throw new IllegalArgumentException("Can not set age for non Ageable plant " + plant.getGrowthConfig());
		}
		Ageable ageable = ((Ageable) block.getBlockData());
		ageable.setAge(stage);
		block.setBlockData(ageable, true);

		return true;
	}

	@Override
	public boolean deleteOnFullGrowth() {
		return false;
	}

}
