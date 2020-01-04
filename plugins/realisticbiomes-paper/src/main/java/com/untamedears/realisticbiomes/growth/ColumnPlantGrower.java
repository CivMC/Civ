package com.untamedears.realisticbiomes.growth;

import com.untamedears.realisticbiomes.utils.RBUtils;
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
		if (getActualHeight(block) < maxHeight) {
			//can grow more
			return 0;
		}
		//fully grown
		return 1;
	}

	protected int getActualHeight(Block block) {
		Block bottom = getRelativeBlock(block, BlockFace.DOWN);
		Block top = getRelativeBlock(block, BlockFace.UP);

		return top.getY() - bottom.getY();
	}

	protected Material getAccordingMaterial(Block block) {
		Material material = RBUtils.getRemappedMaterial(block.getType());
		if (material == null) {
			material = block.getType();
		}

		return material;
	}

	/**
	 * Handles the growth of a column plant ( i.e sugarcane, cactus )
	 * @param block Block of the corresponding plant
	 * @param howMany How tall should the growth be
	 * @return highest plant block
	 */
	protected Block growOnTop(Block block, int howMany) {
		// Turns Bamboo saplings into Bamboo and prevents growing 2 saplings on top of each others
		Material finalType = this.getAccordingMaterial(block);
		if (block.getType() != finalType) {
			block.setType(finalType);
		}

		int counter = 1;
		Block onTop = block;
		while (counter < maxHeight && howMany > 0) {
			counter++;
			onTop = onTop.getRelative(BlockFace.UP);
			Material topMaterial = onTop.getType();
			if (topMaterial == Material.AIR) {
				onTop.setType(finalType);
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

		return onTop.getType() != finalType ? onTop.getRelative(BlockFace.DOWN) : onTop;
	}

	@Override
	public void setStage(Block block, int stage) {
		if (stage == 0) {
			return;
		}
		growOnTop(block, stage);
	}

}
