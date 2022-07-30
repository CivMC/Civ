package com.untamedears.realisticbiomes.growth;

import com.untamedears.realisticbiomes.model.Plant;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Bamboo;

public class BambooGrower extends ColumnPlantGrower {
	protected static final int LEAVES_AMOUNT = 3;
	protected static final int LARGE_LEAVES_START_HEIGHT = 5;

	public BambooGrower(int maxHeight) {
		super(maxHeight, Material.BAMBOO, BlockFace.UP ,false);
	}

	@Override
	protected VerticalGrowResult growVertically(Plant plant, Block block, int howMany) {
		// Actual growth is here:
		VerticalGrowResult result = super.growVertically(plant, block, howMany);
		handleProperLeafGrowth(block, result.top());
		return result;
	}

	@Override
	public int getStage(Plant plant) {
		Block block = plant.getLocation().getBlock();
		if (block.getType() != Material.BAMBOO_SAPLING && block.getType() != Material.BAMBOO) {
			return -1;
		}
		Block bottom = getRelativeBlock(block, getPrimaryGrowthDirection().getOppositeFace());
		if (!bottom.getLocation().equals(block.getLocation())) {
			return -1;
		}
		int stage = getActualHeight(block) - 1;
		return Math.min(stage, getMaxStage());
	}

	private void handleProperLeafGrowth(Block block, Block highestBlock) {
		// according to https://minecraft.gamepedia.com/Bamboo#Appearance
		int actualHeight = super.getActualHeight(block) + 1;
		int leavesLeft = LEAVES_AMOUNT >= actualHeight ? actualHeight - 1 : LEAVES_AMOUNT;
		Block underBlock = highestBlock;
		Bamboo.Leaves leavesType = actualHeight >= LARGE_LEAVES_START_HEIGHT ?
				Bamboo.Leaves.LARGE : Bamboo.Leaves.SMALL;

		// Makes Bamboo growth O(2n) but prevents forking column based growth into similar yet different code.
		while (underBlock.getType() == highestBlock.getType()) {
			Bamboo data = (Bamboo)underBlock.getBlockData();
			if (leavesLeft != 0) {
				data.setLeaves(leavesType);
				leavesLeft--;
				if (leavesLeft == 1) {
					leavesType = Bamboo.Leaves.SMALL;
				}
			} else {
				data.setLeaves(Bamboo.Leaves.NONE);
			}
			underBlock.setBlockData(data);
			underBlock = underBlock.getRelative(BlockFace.DOWN);
		}
	}
}
