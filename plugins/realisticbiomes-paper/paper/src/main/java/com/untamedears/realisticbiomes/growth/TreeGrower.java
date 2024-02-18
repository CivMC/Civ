package com.untamedears.realisticbiomes.growth;

import com.untamedears.realisticbiomes.PlantManager;
import com.untamedears.realisticbiomes.RealisticBiomes;
import com.untamedears.realisticbiomes.model.Plant;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
		case FLOWERING_AZALEA:
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

	/**
	 * Checks whether the block is part of a 2x2 grid and returns the north west block
	 *
	 * @param block to check for
	 * @param mat Sapling material
	 * @return North west block; null if the block is not part of a 2x2 sapling grid
	 */
	private static Block findNWSapling(Block block, Material mat) {
		Block northwest = null;
		for (Block nwCandidate : new Block[] { block, block.getRelative(1, 0, 1), block.getRelative(0, 0, 1),
				block.getRelative(1, 0, 0) }) {
			if (adjacentSaplingCheck(mat, nwCandidate)) {
				northwest = nwCandidate;
				break;
			}
		}
		if (northwest == null) {
			return null;
		}
		return northwest;
	}

	private static void removeSapling(Block block) {
		PlantManager manager = RealisticBiomes.getInstance().getPlantManager();
		Plant plant = manager.getPlant(block);
		if (plant == null) {
			return;
		}
		manager.deletePlant(plant);
		block.setType(Material.AIR);
	}

	/**
	 * Remove a 2x2 saplings grid if the block is part of one
	 *
	 * @param block to check for
	 * @param mat Sapling material
	 */
	private static void clearBigTreeSaplings(Block block, Material mat) {
		Block northwest = null;
		Block northeast, southwest, southeast;
		northwest = findNWSapling(block, mat);
		if (northwest == null) {
			return;
		}

		northeast = northwest.getRelative(BlockFace.EAST);
		southwest = northwest.getRelative(BlockFace.SOUTH);
		southeast = northeast.getRelative(BlockFace.SOUTH);

		removeSapling(northwest);
		removeSapling(northeast);
		removeSapling(southeast);
		removeSapling(southwest);
	}

	private static TreeType remapSaplingToTree(Material mat, boolean big) {
		switch (mat) {
		case OAK_SAPLING:
			return TreeType.TREE;
		case BIRCH_SAPLING:
			return TreeType.BIRCH;
		case ACACIA_SAPLING:
			return TreeType.ACACIA;
		case FLOWERING_AZALEA:
			return TreeType.AZALEA;
		case DARK_OAK_SAPLING:
			return big ? TreeType.DARK_OAK : null;
		case JUNGLE_SAPLING:
			return big ? TreeType.JUNGLE : TreeType.SMALL_JUNGLE;
		case SPRUCE_SAPLING:
			return big ? TreeType.MEGA_REDWOOD : TreeType.REDWOOD;
		case CHORUS_FLOWER:
			return TreeType.CHORUS_PLANT;
		default:
			return null;
		}
	}
	

	public TreeGrower(Material saplingType) {
		super(saplingType, 1, 1);
	}

	@Override
	public int getStage(Plant plant) {
		Block block = plant.getLocation().getBlock();
		if (block.getType() != this.material) {
			return -1;
		}
		return 0;
	}

	@Override
	public boolean setStage(Plant plant, int stage) {
		if (stage < 1) {
			return true;
		}
		Block block = plant.getLocation().getBlock();
		// Re-Read the block data to make sure it is up to date
		if (!(block.getBlockData() instanceof Sapling)) {
			return true;
		}
		Material mat = block.getType();
		boolean canBeBig = canBeBig(mat);
		if (canBeBig) {
			canBeBig = canGrowBig(block, mat);
		}
		TreeType type = remapSaplingToTree(mat, canBeBig);
		if (type == null) {
			return true;
		}
		if (canBeBig) {
			clearBigTreeSaplings(block, mat);
		} else {
			block.setType(Material.AIR);
		}
		if (!block.getLocation().getWorld().generateTree(block.getLocation(), type)) {
			//failed, so restore sapling, TODO restore 2x2
			block.setType(mat);
		}
		return true;
	}
	
	@Override
	public boolean deleteOnFullGrowth() {
		return true;
	}

}
