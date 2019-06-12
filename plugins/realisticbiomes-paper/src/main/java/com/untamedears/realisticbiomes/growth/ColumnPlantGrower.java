package com.untamedears.realisticbiomes.growth;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;

public class ColumnPlantGrower extends AgeableGrower {

	private int maxHeight;

	public ColumnPlantGrower(int maxHeight) {
		super(1, 1);
		this.maxHeight = maxHeight;
	}

	private void growOnTop(Block block, int howMany) {
		int counter = 1;
		Block onTop = block.getRelative(BlockFace.UP);
		while (counter < maxHeight && howMany > 0) {
			counter++;
			Material topMaterial = onTop.getType();
			if (topMaterial == Material.AIR) {
				onTop.setType(block.getType());
				howMany--;
				continue;
			}
			if (topMaterial == block.getType()) {
				// already existing block of the same plant
				continue;
			}
			// neither air, nor the right plant, but something else blocking growth, so we
			// stop
			break;
		}
	}

	@Override
	public void setPersistentProgress(PlantGrowthConfig config, Block block, double progress) {
		if (progress < 1.0) {
			return;
		}
		growOnTop(block, (int) progress);
	}

	@Override
	public void setStage(Block block, int stage) {
		if (stage == 0) {
			return;
		}
		growOnTop(block, 1);
	}

}
