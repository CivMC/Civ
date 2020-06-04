package com.untamedears.realisticbiomes.growth;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.untamedears.realisticbiomes.model.Plant;

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
	private Material finalMaterial;

	public ColumnPlantGrower(int maxHeight) {
		this(maxHeight, null);
	}

	
	public ColumnPlantGrower(int maxHeight, Material finalMaterial) {
		this.maxHeight = maxHeight;
		this.finalMaterial = finalMaterial;
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
	public int getStage(Plant plant) {
		Block block = plant.getLocation().getBlock();
		if (getActualHeight(block) < maxHeight) {
			// can grow more
			return 0;
		}
		// fully grown
		return 1;
	}

	protected int getActualHeight(Block block) {
		Block bottom = getRelativeBlock(block, BlockFace.DOWN);
		Block top = getRelativeBlock(block, BlockFace.UP);

		return top.getY() - bottom.getY();
	}

	/**
	 * Handles the growth of a column plant ( i.e sugarcane, cactus )
	 * 
	 * @param block   Block of the corresponding plant
	 * @param howMany How tall should the growth be
	 * @return highest plant block
	 */
	protected Block growOnTop(Block block, int howMany) {
		if (finalMaterial != null && block.getType() != finalMaterial) {
			block.setType(finalMaterial);
		}

		int counter = 1;
		Block onTop = block;
		while (counter < maxHeight && howMany > 0) {
			counter++;
			onTop = onTop.getRelative(BlockFace.UP);
			Material topMaterial = onTop.getType();
			if (topMaterial == Material.AIR) {
				onTop.setType(finalMaterial);
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

		return onTop.getType() != finalMaterial ? onTop.getRelative(BlockFace.DOWN) : onTop;
	}

	@Override
	public void setStage(Plant plant, int stage) {
		if (stage == 0) {
			return;
		}
		Block block = plant.getLocation().getBlock();
		growOnTop(block, stage);
	}

}
