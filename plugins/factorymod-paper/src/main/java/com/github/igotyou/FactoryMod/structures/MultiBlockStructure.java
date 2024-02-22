package com.github.igotyou.FactoryMod.structures;

import com.github.igotyou.FactoryMod.FactoryMod;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

/**
 * Physical representation of a factory. This may be any shape as long as the
 * required methods can be applied on the shape.
 *
 */
public abstract class MultiBlockStructure {

	/**
	 * Checks all sides of the given block for blocks with the given material
	 * and returns all the blocks which fulfill that criteria
	 * 
	 * @param b
	 *            Block to check around
	 * @param m
	 *            Material which the adjacent block should be
	 * @return All the blocks adjacent to the given block
	 *         and of the given material type
	 */
	public static List<Block> searchForBlockOnAllSides(Block b, Material m) {
		LinkedList<Block> result = new LinkedList<>();
		for (BlockFace face : WorldUtils.ALL_SIDES) {
			Block side = b.getRelative(face);
			if (side.getType() == m) {
				result.add(side);
			}
		}
		return result;
	}

	/**
	 * Checks north, south, east and west of the given block for blocks with the
	 * given material and returns all blocks which fulfill that criteria
	 * 
	 * @param b Block to search around
	 * @param m Material to search for
	 * @return All the blocks adjacent (not above or below) to the given block
	 *         and of the given material type
	 */
	public static List<Block> searchForBlockOnSides(Block b, Material m) {
		LinkedList<Block> result = new LinkedList<>();
		for (BlockFace face : WorldUtils.PLANAR_SIDES) {
			Block side = b.getRelative(face);
			if (side.getType() == m) {
				result.add(side);
			}
		}
		return result;
	}

	/**
	 * @return Whether all parts of this factory are where they should be and no
	 *         block is broken
	 */
	public abstract boolean isComplete();

	/**
	 * Gets all parts of this factory. It is very important to let this have the
	 * same order as a constructor to create a factory based on the given list
	 * of blocks, so this method can be used to dump block locations into the
	 * db, while the constructor can recreate the same factory at a later point
	 * 
	 * @return All blocks which are part of this factory
	 */
	public abstract List<Location> getAllBlocks();

	/**
	 * Rechecks whether all blocks of this factory exists and sets the variable
	 * used for isComplete(), if needed
	 */
	public abstract void recheckComplete();

	/**
	 * @return center block of the factory which it was created from
	 */
	public abstract Location getCenter();

	/**
	 * @return All interaction blocks and blocks that are not allowed to be used
	 *         in two factories at once
	 */
	public abstract List<Block> getRelevantBlocks();

	public static List<Block> getAdjacentBlocks(Block b) {
		List<Block> blocks = new LinkedList<>();
		for (BlockFace face : WorldUtils.ALL_SIDES) {
			blocks.add(b.getRelative(face));
		}
		return blocks;
	}

	/**
	 * Only deals with directly powered redstone interactions, not indirect
	 * power. If all blocks powering this block are on the same group or if the
	 * block is insecure or if the block is unreinforced, true will be returned
	 * 
	 * @param here
	 *            The block to check around.
	 * @return Whether all power sources around the given block are on the same
	 *         group
	 */
	public static boolean citadelRedstoneChecks(Block here) {
		if (!FactoryMod.getInstance().getManager().isCitadelEnabled()) {
			return true;
		}
		Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(here);
		if (rein == null || rein.isInsecure()) {
			return true;
		}
		int prGID = rein.getGroup().getGroupId();
		for (BlockFace face : WorldUtils.ALL_SIDES) {
			Block rel = here.getRelative(face);
			if (here.isBlockFacePowered(face)) {
				Reinforcement relRein = ReinforcementLogic.getReinforcementProtecting(rel);
				if (relRein == null || relRein.getGroup().getGroupId() != prGID) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean blockedByExistingFactory() {
		for (Block b : getRelevantBlocks()) {
			if (FactoryMod.getInstance().getManager().factoryExistsAt(b.getLocation())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether all relevant/interaction blocks of this factory were
	 * completly destroyed/replaced by other blocks
	 * 
	 * @return True if the structure is completly destroyed, false if not
	 */
	public abstract boolean relevantBlocksDestroyed();

}
