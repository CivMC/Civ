package vg.civcraft.mc.civmodcore.api;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Class of utility functions for Blocks, and BlockFaces referencing Blocks around a Block.
 */
public class BlockAPI {
	public static final List<BlockFace> ALL_SIDES = Arrays.asList(BlockFace.UP, BlockFace.DOWN,
			BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST);

	public static final List<BlockFace> PLANAR_SIDES = Arrays.asList(BlockFace.NORTH,
			BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST);

	private static final Predicate<BlockFace> TRUE = f -> true;

	public static List<Block> getBlockSides(Block block, List<BlockFace> faces, Predicate<BlockFace> filter) {
		List<Block> blocks = new ArrayList<>(faces.size());
		for (BlockFace face : faces) {
			if (filter.test(face)) {
				blocks.add(block.getRelative(face));
			}
		}
		return blocks;
	}

	public static List<Block> getPlanarSides(Block block, Predicate<BlockFace> filter) {
		return getBlockSides(block, PLANAR_SIDES, filter);
	}

	public static List<Block> getPlanarSides(Block block) {
		return getPlanarSides(block, TRUE);
	}

	public static List<Block> getAllSides(Block block, Predicate<BlockFace> filter) {
		return getBlockSides(block, ALL_SIDES, filter);
	}

	public static List<Block> getAllSides(Block block) {
		return getAllSides(block, TRUE);
	}
}
