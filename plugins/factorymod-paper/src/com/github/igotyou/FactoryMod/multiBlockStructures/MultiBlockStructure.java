package com.github.igotyou.FactoryMod.multiBlockStructures;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 * Physical representation of a factory. This may be any shape as long as the
 * required methods can be applied on the shape.
 *
 */
public abstract class MultiBlockStructure {
	protected static BlockFace[] allBlockSides = new BlockFace[] {
			BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST,
			BlockFace.SOUTH, BlockFace.NORTH };
	protected static BlockFace[] northEastWestSouthSides = new BlockFace[] {
			BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH };

	/**
	 * Checks east,west,north and south of the given block for other blocks with
	 * the given material and returns all the blocks which fulfill that criteria
	 * 
	 * @param b
	 *            Block to check around
	 * @param m
	 *            Material which the adjacent block should be
	 * @return All the blocks adjacent (not above or below) to the given block
	 *         and of the given material type
	 */
	public static List<Block> searchForBlockOnSides(Block b, Material m) {
		LinkedList<Block> result = new LinkedList<Block>();
		for (BlockFace face : allBlockSides) {
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
	public abstract List<Block> getAllBlocks();

	/**
	 * @return center block of the factory which it was created from
	 */
	public abstract Location getCenter();

}
