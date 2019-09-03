package vg.civcraft.mc.civmodcore.api;

import com.google.common.collect.ImmutableList;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Class of utility functions for Blocks, and BlockFaces referencing Blocks around a Block.
 */
public class BlockAPI {
	public static final List<BlockFace> ALL_SIDES = ImmutableList.of(BlockFace.UP, BlockFace.DOWN,
			BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST);

	public static final List<BlockFace> PLANAR_SIDES = ImmutableList.of(BlockFace.NORTH,
			BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST);

	private static final Predicate<BlockFace> TRUE = f -> true;

	/**
	 * Get all blocks on specified faces of the block, where the face also matches the filter
	 * @param block the block to get surrounding blocks of
	 * @param faces the block faces to get relative to the block
	 * @param filter the filter the block faces must matches
	 * @return the resulting blocks
	 */
	public static List<Block> getBlockSides(Block block, List<BlockFace> faces, Predicate<BlockFace> filter) {
		List<Block> blocks = new ArrayList<>(faces.size());
		for (BlockFace face : faces) {
			if (filter.test(face)) {
				blocks.add(block.getRelative(face));
			}
		}
		return blocks;
	}

	/**
	 * Gets all blocks on every cardinal side to the block, where the block face matches the filter
	 * @param block the block
	 * @param filter the filter to check block faces against
	 * @return all blocks on every cardinal side of the block where the relative block face matches the filter
	 */
	public static List<Block> getPlanarSides(Block block, Predicate<BlockFace> filter) {
		return getBlockSides(block, PLANAR_SIDES, filter);
	}

	/**
	 * Get all blocks on every cardinal side to the specified block
	 * @param block the block
	 * @return blocks on every cardinal side
	 */
	public static List<Block> getPlanarSides(Block block) {
		return getPlanarSides(block, TRUE);
	}

	/**
	 * Gets all blocks next to the block, where the block face matches the filter
	 * @param block the block
	 * @param filter the filter to check block faces against
	 * @return all blocks next to the specified block where the relative block face matches the filter
	 */
	public static List<Block> getAllSides(Block block, Predicate<BlockFace> filter) {
		return getBlockSides(block, ALL_SIDES, filter);
	}

	/**
	 * Gets all blocks next to the specified block
	 * @param block the block
	 * @return all blocks next to the block
	 */
	public static List<Block> getAllSides(Block block) {
		return getAllSides(block, TRUE);
	}

	/**
	 * Returns a mutable list of all sides of a block
	 * @return all sides of a block: north, east, south, west, up down
	 */
	public static List<BlockFace> getAllSides() {
		return new ArrayList<>(ALL_SIDES);
	}

	/**
	 * Returns a mutable list of all planar sides of a block
	 * @return all planar sides of a block: north, east, west, south
	 */
	public static List<BlockFace> getPlanarSides() {
		return new ArrayList<>(PLANAR_SIDES);
	}
}
