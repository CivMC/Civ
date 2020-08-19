package com.untamedears.realisticbiomes.growth;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.untamedears.realisticbiomes.model.Plant;

public class VerticalBlockSpreadGrower extends IArtificialGrower {

	private final int maximumInArea;
	private final int maximumRange;
	private Material material;

	public VerticalBlockSpreadGrower(Material material, int maximumInArea, int maximumRange) {
		this.maximumInArea = maximumInArea;
		this.maximumRange = maximumRange;
	}

	@Override
	public int getIncrementPerStage() {
		return 1;
	}

	@Override
	public int getMaxStage() {
		return maximumInArea - 1;
	}

	@Override
	public int getStage(Plant plant) {
		Block block = plant.getLocation().getBlock();
		if (block.getType() != material) {
			return -1;
		}
		int maxX = block.getX() + maximumRange;
		int maxZ = block.getZ() + maximumRange;
		int count = 0;
		for (int x = block.getX() - maximumRange; x < maxX; x++) {
			for (int z = block.getZ() - maximumRange; z < maxZ; z++) {
				if (block.getWorld().getBlockAt(x, block.getY(), z).getType() == material) {
					count++;
				}
			}
		}
		// remove block itself which is always there, because stage 0 equals only the
		// center block itself
		return count - 1;
	}

	@Override
	public void setStage(Plant plant, int stage) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean deleteOnFullGrowth() {
		return false;
	}

}
