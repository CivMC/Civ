package vg.civcraft.mc.civmodcore.api;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.DoubleChestInventory;
import vg.civcraft.mc.civmodcore.util.Iteration;
import vg.civcraft.mc.civmodcore.util.NullCoalescing;

/**
 * Class of utility functions for Blocks, and BlockFaces referencing Blocks around a Block.
 */
public final class BlockAPI {

	public static final List<BlockFace> ALL_SIDES = ImmutableList.of(
			BlockFace.UP,
			BlockFace.DOWN,
			BlockFace.NORTH,
			BlockFace.SOUTH,
			BlockFace.WEST,
			BlockFace.EAST);

	public static final List<BlockFace> PLANAR_SIDES = ImmutableList.of(
			BlockFace.NORTH,
			BlockFace.SOUTH,
			BlockFace.WEST,
			BlockFace.EAST);

	private static final Predicate<BlockFace> TRUE = f -> true;

	/**
	 * Checks whether this block is valid and so can be handled reasonably without error.
	 *
	 * @param block The block to check.
	 * @return Returns true if the block is valid.
	 *
	 * @apiNote This will return true even if the block is air. Use {@link MaterialAPI#isAir(Material)} as an additional
	 *         check if this is important to you.
	 */
	@SuppressWarnings("ConstantConditions")
	public static boolean isValidBlock(Block block) {
		if (block == null) {
			return false;
		}
		if (block.getType() == null) {
			return false;
		}
		if (!LocationAPI.isValidLocation(block.getLocation())) {
			return false;
		}
		return true;
	}

	/**
	 * Returns a map of a block's relatives.
	 *
	 * @param block The block to get the relatives of.
	 * @param faces An array of the faces, which will be the keys of the returned map.
	 * @return Returns an immutable map of the block's relatives.
	 */
	public static Map<BlockFace, Block> getBlockSides(Block block, BlockFace... faces) {
		if (faces == null || faces.length < 1) {
			return Collections.unmodifiableMap(new EnumMap<>(BlockFace.class));
		}
		else {
			return getBlockSides(block, Arrays.asList(faces));
		}
	}

	/**
	 * Returns a map of a block's relatives.
	 *
	 * @param block The block to get the relatives of.
	 * @param faces A collection of the faces, which will be the keys of the returned map.
	 * @return Returns an immutable map of the block's relatives.
	 */
	public static Map<BlockFace, Block> getBlockSides(Block block, Collection<BlockFace> faces) {
		EnumMap<BlockFace, Block> results = new EnumMap<>(BlockFace.class);
		if (block != null && faces != null) {
			faces.forEach((face) -> results.put(face, block.getRelative(face)));
		}
		return Collections.unmodifiableMap(results);
	}

	/**
	 * Attempts to get the other block of a double chest.
	 *
	 * @param chest The block that represents the double chest block you already have.
	 * @return Returns the other block or null if none can be found, or if the given block isn't that of a double chest.
	 */
	public static Block getOtherDoubleChestBlock(Block chest) {
		if (!isValidBlock(chest)) {
			return null;
		}
		DoubleChestInventory inventory = NullCoalescing.chain(() ->
				(DoubleChestInventory) ((Chest) chest.getState()).getInventory());
		if (inventory == null) {
			return null;
		}
		Location other = Iteration.other(chest.getLocation(),
				inventory.getLeftSide().getLocation(),
				inventory.getRightSide().getLocation());
		if (other == null) {
			return null;
		}
		return other.getBlock();
	}

	// ------------------------------------------------------------
	// Deprecated
	// ------------------------------------------------------------

	/**
	 * Get all blocks on specified faces of the block, where the face also matches the filter.
	 *
	 * @param block the block to get surrounding blocks of
	 * @param faces the block faces to get relative to the block
	 * @param filter the filter the block faces must matches
	 * @return the resulting blocks
	 */
	@Deprecated
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
	 *
	 * @param block the block
	 * @param filter the filter to check block faces against
	 * @return all blocks on every cardinal side of the block where the relative block face matches the filter
	 */
	@Deprecated
	public static List<Block> getPlanarSides(Block block, Predicate<BlockFace> filter) {
		return getBlockSides(block, PLANAR_SIDES, filter);
	}

	/**
	 * Get all blocks on every cardinal side to the specified block
	 *
	 * @param block the block
	 * @return blocks on every cardinal side
	 */
	@Deprecated
	public static List<Block> getPlanarSides(Block block) {
		return getPlanarSides(block, TRUE);
	}

	/**
	 * Gets all blocks next to the block, where the block face matches the filter
	 *
	 * @param block the block
	 * @param filter the filter to check block faces against
	 * @return all blocks next to the specified block where the relative block face matches the filter
	 */
	@Deprecated
	public static List<Block> getAllSides(Block block, Predicate<BlockFace> filter) {
		return getBlockSides(block, ALL_SIDES, filter);
	}

	/**
	 * Gets all blocks next to the specified block
	 *
	 * @param block the block
	 * @return all blocks next to the block
	 */
	@Deprecated
	public static List<Block> getAllSides(Block block) {
		return getAllSides(block, TRUE);
	}

	/**
	 * Returns a mutable list of all sides of a block
	 *
	 * @return all sides of a block: north, east, south, west, up down
	 */
	@Deprecated
	public static List<BlockFace> getAllSides() {
		return new ArrayList<>(ALL_SIDES);
	}

	/**
	 * Returns a mutable list of all planar sides of a block
	 *
	 * @return all planar sides of a block: north, east, west, south
	 */
	@Deprecated
	public static List<BlockFace> getPlanarSides() {
		return new ArrayList<>(PLANAR_SIDES);
	}

}
