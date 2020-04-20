package vg.civcraft.mc.civmodcore.api;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
	
	private BlockAPI() {
	}

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

	/**
	 * Checks whether this block is valid and so can be handled reasonably without error.
	 *
	 * @param block The block to check.
	 * @return Returns true if the block is valid.
	 *
	 * @apiNote This will return true even if the block is air. Use {@link MaterialAPI#isAir(Material)} as an additional
	 *         check if this is important to you.
	 */
	public static boolean isValidBlock(Block block) {
		if (block == null) {
			return false;
		}
		if (block.getType() == null) { // Do not remove this, it's not necessarily certain
			return false;
		}
		return LocationAPI.isValidLocation(block.getLocation());
	}

	/**
	 * Returns a map of a block's relatives.
	 *
	 * @param block The block to get the relatives of.
	 * @param faces An array of the faces, which will be the keys of the returned map.
	 * @return Returns an immutable map of the block's relatives.
	 */
	public static Map<BlockFace, Block> getBlockSidesMapped(Block block, BlockFace... faces) {
		if (faces == null || faces.length < 1) {
			return Collections.unmodifiableMap(new EnumMap<>(BlockFace.class));
		}
		else {
			return getBlockSidesMapped(block, Arrays.asList(faces));
		}
	}

	/**
	 * Returns a map of a block's relatives.
	 *
	 * @param block The block to get the relatives of.
	 * @param faces A collection of the faces, which will be the keys of the returned map.
	 * @return Returns an immutable map of the block's relatives.
	 */
	public static Map<BlockFace, Block> getBlockSidesMapped(Block block, Collection<BlockFace> faces) {
		EnumMap<BlockFace, Block> results = new EnumMap<>(BlockFace.class);
		if (block != null && faces != null) {
			faces.forEach(face -> results.put(face, block.getRelative(face)));
		}
		return Collections.unmodifiableMap(results);
	}
	
	/**
	 * Returns a map of a block's relatives.
	 *
	 * @param block The block to get the relatives of.
	 * @param faces A collection of the faces, which will be the keys of the returned map.
	 * @return Returns an immutable map of the block's relatives.
	 */
	public static List<Block> getBlockSides(Block block, Collection<BlockFace> faces) {
		if (block == null || faces == null) {
            throw new IllegalArgumentException("One of the args passed was null");
        }
		if (faces.isEmpty()) {
			return Collections.emptyList();
		}
        return faces.stream().map(block::getRelative).collect(Collectors.toList());
	}

	/**
	 * Returns a map of all the block's relatives.
	 *
	 * @param block The block to get all the relatives of.
	 * @return Returns an immutable map of all the block's relatives.
	 */
	public static Map<BlockFace, Block> getAllSidesMapped(Block block) {
		return getBlockSidesMapped(block, ALL_SIDES);
	}
	
	/**
	 * Returns a list of all the block's relatives.
	 *
	 * @param block The block to get all the relatives of.
	 * @return Returns an immutable list of all the block's relatives.
	 */
	public static List<Block> getAllSides(Block block) {
		return getBlockSides(block, ALL_SIDES);
	}

	/**
	 * Returns a map of all the block's planar relatives.
	 *
	 * @param block The block to get the planar relatives of.
	 * @return Returns an immutable map of all the block's planar relatives.
	 */
	public static Map<BlockFace, Block> getPlanarSidesMapped(Block block) {
		return getBlockSidesMapped(block, PLANAR_SIDES);
	}
	
	/**
	 * Returns a list of all the block's planar relatives.
	 *
	 * @param block The block to get the planar relatives of.
	 * @return Returns an immutable list of all the block's planar relatives.
	 */
	public static List<Block> getPlanarSides(Block block) {
		return getBlockSides(block, PLANAR_SIDES);
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

}
