package com.untamedears.realisticbiomes.growth;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class ColumnPlantGrower extends IArtificialGrower {

	public static Block getRelativeBlock(Block block, BlockFace face) {
		Material mat = block.getType();
		Block bottomBlock = block;
		// not actually using this variable, but just having it here as a fail safe
		for (int i = 0; i < 257; i++) {
			Block below = bottomBlock.getRelative(face);
			if (below.getType() != mat) {
				break;
			}
			bottomBlock = below;
		}
		return bottomBlock;
	}

	private int maxHeight;

	public ColumnPlantGrower(int maxHeight) {
		this.maxHeight = maxHeight;
	}

	@Override
	public int getIncrementPerStage() {
		return 1;
	}

	@Override
	public int getMaxStage() {
		return 1;
	}

	@Override
	public int getStage(Block block) {
		Block bottom = getRelativeBlock(block, BlockFace.DOWN);
		Block top = getRelativeBlock(block, BlockFace.UP);
		if (top.getY() - bottom.getY() < maxHeight) {
			//can grow more
			return 0;
		}
		//fully grown
		return 1;
	}

	private void growOnTop(Block block, int howMany) {
		int counter = 1;
		Block onTop = block;
		while (counter < maxHeight && howMany > 0) {
			counter++;
			onTop = onTop.getRelative(BlockFace.UP);
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
	public void setStage(Block block, int stage) {
		if (stage == 0) {
			return;
		}
		growOnTop(block, stage);
	}

}
