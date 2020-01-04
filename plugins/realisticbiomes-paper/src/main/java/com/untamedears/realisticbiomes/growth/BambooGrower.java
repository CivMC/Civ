package com.untamedears.realisticbiomes.growth;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Bamboo;

public class BambooGrower extends ColumnPlantGrower {
	protected final int LEAVES_AMOUNT = 3;
	protected final int LARGE_LEAVES_START_HEIGHT = 5;

	public BambooGrower(int maxHeight) {
		super(maxHeight);
	}

	@Override
	protected Block growOnTop(Block block, int howMany) {
		// Actual growth is here:
		Block highestBlock = super.growOnTop(block, howMany);

		// Leaves growth is here:
		//     according to https://minecraft.gamepedia.com/Bamboo#Appearance
		Block underBlock = highestBlock;
		Bamboo.Leaves leavesType = super.getActualHeight(block) > LARGE_LEAVES_START_HEIGHT ?
				Bamboo.Leaves.LARGE : Bamboo.Leaves.SMALL;
		int leavesLeft = LEAVES_AMOUNT;

		// Makes Bamboo growth O(2n) but prevents forking column based growth into similar yet different code.
		while (underBlock.getType() != highestBlock.getType()) {
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

		return highestBlock;
	}
}
