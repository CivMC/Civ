package com.untamedears.realisticbiomes.growth;

import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Sapling;

public class TreeGrower extends AgeableGrower {

	private static boolean adjacentSaplingCheck(Material mat, Block northwest) {
		for (Block block : new Block[] { northwest, northwest.getRelative(1, 0, 0), northwest.getRelative(0, 0, 1),
				northwest.getRelative(1, 0, 1) }) {
			if (block == null) {
				return false;
			}
			if (block.getType() != mat) {
				return false;
			}
		}
		return true;
	}

	private static boolean canBeBig(Material mat) {
		switch (mat) {
		case OAK_SAPLING:
		case BIRCH_SAPLING:
		case ACACIA_SAPLING:
		case CHORUS_FLOWER:
			return false;
		case DARK_OAK_SAPLING:
		case JUNGLE_SAPLING:
		case SPRUCE_SAPLING:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Checks whether the block is part of a valid 2x2 grid
	 * 
	 * @param block Block to check for
	 * @param mat   Sapling material
	 * @return True if the block is part of a 2x2 sapling grid and could grow large,
	 *         false otherwise
	 */
	private static boolean canGrowBig(Block block, Material mat) {
		return adjacentSaplingCheck(mat, block.getRelative(-1, 0, -1))
				|| adjacentSaplingCheck(mat, block.getRelative(0, 0, -1))
				|| adjacentSaplingCheck(mat, block.getRelative(-1, 0, 0)) || adjacentSaplingCheck(mat, block);
	}

	private static TreeType remapSaplingToTree(Material mat, boolean big) {
		switch (mat) {
		case OAK_SAPLING:
			return TreeType.TREE;
		case BIRCH_SAPLING:
			return TreeType.BIRCH;
		case ACACIA_SAPLING:
			return TreeType.ACACIA;
		case DARK_OAK_SAPLING:
			return big ? TreeType.DARK_OAK : null;
		case JUNGLE_SAPLING:
			return big ? TreeType.JUNGLE : null;
		case SPRUCE_SAPLING:
			return big ? TreeType.TALL_REDWOOD : TreeType.MEGA_REDWOOD;
		case CHORUS_FLOWER:
			return TreeType.CHORUS_PLANT;
		default:
			return null;
		}
	}

	public TreeGrower() {
		super(1, 1);
	}

	@Override
	public int getStage(Block block) {
		if (!(block.getBlockData() instanceof Sapling)) {
			return -1;
		}
		return 0;
	}

	@Override
	public void setStage(Block block, int stage) {
		if (stage < 1) {
			return;
		}
		if (!(block.getBlockData() instanceof Sapling)) {
			return;
		}
		Material mat = block.getType();
		boolean canBeBig = canBeBig(mat);
		if (canBeBig) {
			canBeBig = canGrowBig(block, mat);
		}
		TreeType type = remapSaplingToTree(mat, canBeBig);
		if (type == null) {
			return;
		}
		block.setType(Material.AIR);
		block.getLocation().getWorld().generateTree(block.getLocation(), type);
	}

}
