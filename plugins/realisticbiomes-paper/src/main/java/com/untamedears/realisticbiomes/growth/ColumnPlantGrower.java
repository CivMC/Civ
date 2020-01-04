package com.untamedears.realisticbiomes.growth;

import com.untamedears.realisticbiomes.utils.RBUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Bamboo;

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

	private int getActualHeight(Block block) {
		Block bottom = getRelativeBlock(block, BlockFace.DOWN);
		Block top = getRelativeBlock(block, BlockFace.UP);

		return top.getY() - bottom.getY();
	}

	private Material getAccordingMaterial(Block block) {
		Material material = RBUtils.getRemappedMaterial(block.getType());
		if (material == null) {
			material = block.getType();
		}

		return material;
	}

	private void growOnTop(Block block, int howMany) {
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

		if (finalType == Material.BAMBOO) {
			handleBambooLeaves(block, onTop, finalType);
		}
	}

	private void handleBambooLeaves(Block block, Block top, Material finalType) {
		// Leaves work according to https://minecraft.gamepedia.com/Bamboo#Appearance
		Block underBlock = top.getRelative(BlockFace.DOWN);
		Bamboo.Leaves leavesType = getActualHeight(block) > 5 ? Bamboo.Leaves.LARGE : Bamboo.Leaves.SMALL;
		int leavesLeft = 3;

		// Makes Bamboo growth O(2n) but prevents other plants to suffer from Bamboo leaf checks.
		while (underBlock.getType() != finalType) {
			if (leavesLeft != 0) {
				((Bamboo) underBlock.getBlockData()).setLeaves(leavesType);
				leavesLeft--;
				if (leavesLeft == 1) {
					leavesType = Bamboo.Leaves.SMALL;
				}
			} else {
				((Bamboo) underBlock.getBlockData()).setLeaves(Bamboo.Leaves.NONE);
			}
			underBlock = underBlock.getRelative(BlockFace.DOWN);
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
