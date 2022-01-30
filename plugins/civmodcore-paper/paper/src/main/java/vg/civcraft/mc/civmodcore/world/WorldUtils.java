package vg.civcraft.mc.civmodcore.world;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Switch;
import org.bukkit.util.BlockIterator;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MoreTags;
import vg.civcraft.mc.civmodcore.utilities.NullUtils;

/**
 * Class of utility functions for Blocks, and BlockFaces referencing Blocks around a Block.
 */
public final class WorldUtils {

	public static final Set<BlockFace> ALL_SIDES = ImmutableSet.of(
			BlockFace.UP,
			BlockFace.DOWN,
			BlockFace.NORTH,
			BlockFace.EAST,
			BlockFace.SOUTH,
			BlockFace.WEST);

	public static final Set<BlockFace> PLANAR_SIDES = ImmutableSet.of(
			BlockFace.NORTH,
			BlockFace.EAST,
			BlockFace.SOUTH,
			BlockFace.WEST);

	public static final Set<BlockFace> VERTICAL_SIDES = ImmutableSet.of(
			BlockFace.UP,
			BlockFace.DOWN);

	/**
	 * Determines whether a location is valid and safe to use.
	 *
	 * @param location The location to check.
	 * @return Returns true if the location exists, is valid, and safe to use.
	 */
	public static boolean isValidLocation(final Location location) {
		if (location == null) {
			return false;
		}
		if (!location.isWorldLoaded()) {
			return false;
		}
		return true;
	}

	/**
	 * Retrieves the world from a location.
	 *
	 * @param location The location to retrieve the world from.
	 * @return Returns the world if loaded, or null.
	 */
	public static World getLocationWorld(final Location location) {
		if (location == null) {
			return null;
		}
		return location.isWorldLoaded() ? location.getWorld() : null;
	}

	/**
	 * Converts a location into a block location. (Yaw and Pitch values are lost)
	 *
	 * @param location The location to convert.
	 * @return Returns a block location, or null if the given location was null.
	 */
	public static Location getBlockLocation(final Location location) {
		if (location == null) {
			return null;
		}
		return new Location(getLocationWorld(location),
				location.getBlockX(),
				location.getBlockY(),
				location.getBlockZ());
	}

	/**
	 * Converts a location into a block's mid point. (Yaw and Pitch values are lost)
	 *
	 * @param location The location to convert.
	 * @return Returns a block's mid point, or null if the given location was null.
	 */
	public static Location getMidBlockLocation(final Location location) {
		if (location == null) {
			return null;
		}
		return getBlockLocation(location).add(0.5d, 0.5d, 0.5d);
	}

	/**
	 * Determines whether a series of locations share the same world.
	 *
	 * @return Returns true if the two locations are not null and share the same world.
	 */
	public static boolean doLocationsHaveSameWorld(final Location former, final Location latter) {
		return NullUtils.equalsNotNull(getLocationWorld(former), getLocationWorld(latter));
	}

	/**
	 * Returns the largest axis distance.
	 *
	 * @param latter The first location.
	 * @param former The second location.
	 * @param consider2D Whether only the X and Z axis should be considered. (true if yes)
	 * @return Returns the largest axis distance, or -1 if there's a problem,
	 *     like the two locations being in two different worlds.
	 */
	public static int blockDistance(final Location former, final Location latter, final boolean consider2D) {
		if (!doLocationsHaveSameWorld(former, latter)) {
			return -1;
		}
		final int x = Math.abs(former.getBlockX() - latter.getBlockX());
		final int z = Math.abs(former.getBlockZ() - latter.getBlockZ());
		if (consider2D) {
			return Math.max(x, z);
		}
		else {
			final int y = Math.abs(former.getBlockY() - latter.getBlockY());
			return Math.max(x, Math.max(y, z));
		}
	}

	/**
	 * Checks whether a location's Y coordinate is a valid block location.
	 *
	 * @param location The location to check.
	 * @return Returns true if the Y coordinate is a valid block location. (False if given location is null!)
	 */
	public static boolean isWithinBounds(final Location location) {
		final World world = getLocationWorld(location);
		if (world == null) {
			return false;
		}
		return location.getY() >= world.getMinHeight() && location.getY() < world.getMaxHeight();
	}


	/**
	 * Determines if a world is currently loaded.
	 *
	 * @param world World to test.
	 * @return Returns true if the world is loaded.
	 */
	public static boolean isWorldLoaded(final World world) {
		if (world == null) {
			return false;
		}
		// Same method in Location.isWorldLoaded()
		return Bukkit.getWorld(world.getUID()) != null;
	}

	/**
	 * Determines if a chunk is loaded in an efficient manner without loading any chunks.
	 *
	 * @param world The world the target chunk is located within.
	 * @param x The (CHUNK) X coordinate.
	 * @param z The (CHUNK) Z coordinate.
	 * @return Returns true if the chunk is loaded.
	 */
	public static boolean isChunkLoaded(final World world, final int x, final int z) {
		if (!isWorldLoaded(world)) {
			return false;
		}
		return world.isChunkLoaded(x, z);
	}

	/**
	 * Retrieves a chunk only if it's loaded.
	 *
	 * @param world The world which the target chunk is located within.
	 * @param x The (CHUNK) X coordinate.
	 * @param z The (CHUNK) Z coordinate.
	 * @return Returns the loaded chunk, or null.
	 */
	public static Chunk getLoadedChunk(final World world, final int x, final int z) {
		if (!isChunkLoaded(world, x, z)) {
			return null;
		}
		return world.getChunkAt(x, z);
	}

	/**
	 * Determines if a block is loaded by nature of whether the chunk it's in is loaded.
	 *
	 * @param location The block location.
	 * @return Returns true if the block is loaded.
	 */
	public static boolean isBlockLoaded(final Location location) {
		if (!isValidLocation(location)) {
			return false;
		}
		final World world = Objects.requireNonNull(location.getWorld());
		final int chunkX = location.getBlockX() >> 4;
		final int chunkZ = location.getBlockZ() >> 4;
		return world.isChunkLoaded(chunkX, chunkZ);
	}

	/**
	 * Retrieves a block only if it's loaded.
	 *
	 * @param world The world which the target block is located within.
	 * @param x The (BLOCK) X coordinate.
	 * @param y The (BLOCK) Y coordinate.
	 * @param z The (BLOCK) Z coordinate.
	 * @return Returns the loaded block, or null.
	 */
	public static Block getLoadedBlock(final World world, final int x, final int y, final int z) {
		if (!isChunkLoaded(world, x >> 4, z >> 4)) {
			return null;
		}
		return world.getBlockAt(x, y, z);
	}

	/**
	 * <p>Checks whether this block is valid and so can be handled reasonably without error.</p>
	 *
	 * <p>Note: This will return true even if the block is air. Use {@link MaterialUtils#isAir(Material)} as an
	 * additional check if this is important to you.</p>
	 *
	 * @param block The block to check.
	 * @return Returns true if the block is valid.
	 */
	public static boolean isValidBlock(final Block block) {
		if (block == null) {
			return false;
		}
		if (block.getType() == null) { // Do not remove this, it's not necessarily certain
			return false;
		}
		return isValidLocation(block.getLocation());
	}

	/**
	 * Safely retrieves a block relative to another block, only returning a block if it's loaded.
	 *
	 * @param block The block to originate from.
	 * @param face The direction to search towards.
	 * @param distance The distance to search for.
	 * @return Returns a [void] block if loaded, or null if not.
	 */
	public static Block safeBlockRelative(final Block block, final BlockFace face, final int distance) {
		if (block == null || face == null || face == BlockFace.SELF || distance == 0) {
			return block;
		}
		final World world = block.getWorld();
		final int x = block.getX() + (face.getModX() * distance);
		final int y = block.getY() + (face.getModY() * distance);
		final int z = block.getZ() + (face.getModZ() * distance);
		return getLoadedBlock(world, x, y, z);
	}

	/**
	 * Returns a map of a block's relatives.
	 *
	 * @param block The block to get the relatives of.
	 * @param faces A collection of the faces, which will be the keys of the returned map.
	 * @return Returns an immutable map of the block's relatives.
	 */
	public static Map<BlockFace, Block> getBlockSidesMapped(final Block block,
															final Collection<BlockFace> faces,
															final boolean forceLoad) {
		final EnumMap<BlockFace, Block> results = new EnumMap<>(BlockFace.class);
		if (isValidBlock(block) && !CollectionUtils.isEmpty(faces)) {
			for (final BlockFace face : faces) {
				if (face == null || face == BlockFace.SELF) {
					continue;
				}
				if (forceLoad) {
					results.put(face, block.getRelative(face));
				}
				else {
					results.put(face, safeBlockRelative(block, face, 1));
				}
			}
		}
		return Collections.unmodifiableMap(results);
	}

	/**
	 * Returns a list of a block's relatives.
	 *
	 * @param block The block to get the relatives of.
	 * @param faces A collection of the faces, which will be the keys of the returned map.
	 * @return Returns an immutable list of the block's relatives.
	 */
	public static List<Block> getBlockSides(final Block block,
											final Collection<BlockFace> faces,
											final boolean forceLoad) {
		final List<Block> results = new ArrayList<>();
		if (isValidBlock(block) && !CollectionUtils.isEmpty(faces)) {
			for (final BlockFace face : faces) {
				if (face == null || face == BlockFace.SELF) {
					continue;
				}
				if (forceLoad) {
					results.add(block.getRelative(face));
				}
				else {
					results.add(safeBlockRelative(block, face, 1));
				}
			}
		}
		return Collections.unmodifiableList(results);
	}

	/**
	 * Returns a map of all the block's direct relatives.
	 *
	 * @param block The block to get the relatives of.
	 * @return Returns an immutable map of the block's relatives.
	 */
	public static Map<BlockFace, Block> getAllBlockSidesMapped(final Block block, final boolean forceLoad) {
		return getBlockSidesMapped(block, ALL_SIDES, forceLoad);
	}

	/**
	 * Returns a list of all the block's direct relatives.
	 *
	 * @param block The block to get the relatives of.
	 * @return Returns an immutable list of the block's relatives.
	 */
	public static List<Block> getAllBlockSides(final Block block, final boolean forceLoad) {
		return getBlockSides(block, ALL_SIDES, forceLoad);
	}

	/**
	 * Returns a map of all the block's planar relatives.
	 *
	 * @param block The block to get the relatives of.
	 * @return Returns an immutable map of the block's relatives.
	 */
	public static Map<BlockFace, Block> getPlanarBlockSidesMapped(final Block block, final boolean forceLoad) {
		return getBlockSidesMapped(block, PLANAR_SIDES, forceLoad);
	}

	/**
	 * Returns a list of all the block's planar relatives.
	 *
	 * @param block The block to get the relatives of.
	 * @return Returns an immutable list of the block's relatives.
	 */
	public static List<Block> getPlanarBlockSides(final Block block, final boolean forceLoad) {
		return getBlockSides(block, PLANAR_SIDES, forceLoad);
	}

	/**
	 * Returns a map of all the block's vertical relatives.
	 *
	 * @param block The block to get the relatives of.
	 * @return Returns an immutable map of the block's relatives.
	 */
	public static Map<BlockFace, Block> getVerticalBlockSidesMapped(final Block block, final boolean forceLoad) {
		return getBlockSidesMapped(block, VERTICAL_SIDES, forceLoad);
	}

	/**
	 * Returns a list of all the block's vertical relatives.
	 *
	 * @param block The block to get the relatives of.
	 * @return Returns an immutable list of the block's relatives.
	 */
	public static List<Block> getVerticalBlockSides(final Block block, final boolean forceLoad) {
		return getBlockSides(block, VERTICAL_SIDES, forceLoad);
	}

	/**
	 * Turns once in a clockwise direction.
	 *
	 * @param face The starting face, which must exist and be planar.
	 * @return Returns the next planar face in a clockwise direction.
	 *
	 * @exception IllegalArgumentException Throws if the given face is null or non-planar.
	 */
	public static BlockFace turnClockwise(final BlockFace face) {
		Preconditions.checkArgument(face != null);
		Preconditions.checkArgument(PLANAR_SIDES.contains(face));
		switch (face) {
			default:
			case NORTH:
				return BlockFace.EAST;
			case EAST:
				return BlockFace.SOUTH;
			case SOUTH:
				return BlockFace.WEST;
			case WEST:
				return BlockFace.NORTH;
		}
	}

	/**
	 * Turns once in a anti-clockwise direction.
	 *
	 * @param face The starting face, which must exist and be planar.
	 * @return Returns the next planar face in a anti-clockwise direction.
	 *
	 * @exception IllegalArgumentException Throws if the given face is null or non-planar.
	 */
	public static BlockFace turnAntiClockwise(final BlockFace face) {
		Preconditions.checkArgument(face != null);
		Preconditions.checkArgument(PLANAR_SIDES.contains(face));
		switch (face) {
			default:
			case NORTH:
				return BlockFace.WEST;
			case EAST:
				return BlockFace.NORTH;
			case SOUTH:
				return BlockFace.EAST;
			case WEST:
				return BlockFace.SOUTH;
		}
	}

	/**
	 * Gets the {@link BlockFace} this attachable is attached to. This exists as
	 * {@link org.bukkit.block.data.Directional} has odd behaviour whereby if attached to the top or bottom of a block,
	 * the direction is the rotation of the block, rather than the attached face.
	 *
	 * @param attachable The Switch, which is an instance of {@link BlockData}. So do your own checks beforehand.
	 * @return Returns the block face the given attachable is attached to, or null.
	 */
	public static BlockFace getAttachedFace(final Switch attachable) {
		if (attachable == null) {
			return null;
		}
		switch (attachable.getAttachedFace()) {
			case CEILING:
				return BlockFace.DOWN;
			case FLOOR:
				return BlockFace.UP;
			case WALL:
				return attachable.getFacing().getOppositeFace();
			default:
				return null;
		}
	}

	/**
	 * Attempts to get the other block of a double chest.
	 *
	 * @param block The block that represents the double chest block you already have.
	 * @return Returns the other block or null if none can be found, or if the given block isn't that of a double chest.
	 */
	public static Block getOtherDoubleChestBlock(final Block block, final boolean forceLoad) {
		if (!isValidBlock(block)) {
			return null;
		}
		final BlockData blockData = block.getBlockData();
		if (!(blockData instanceof Chest)) {
			return null;
		}
		final Chest chestData = (Chest) blockData;
		final Chest.Type side = chestData.getType();
		if (side == Chest.Type.LEFT) {
			final BlockFace relative = turnClockwise(chestData.getFacing());
			if (forceLoad) {
				return block.getRelative(relative);
			}
			else {
				return safeBlockRelative(block, relative, 1);
			}
		}
		else if (side == Chest.Type.RIGHT) {
			final BlockFace relative = turnAntiClockwise(chestData.getFacing());
			if (forceLoad) {
				return block.getRelative(relative);
			}
			else {
				return safeBlockRelative(block, relative, 1);
			}
		}
		else {
			return null;
		}
	}

	/**
	 * Creates a {@link BlockIterator} from a block's perspective, which is lacking from its constructors, which are
	 * more focused on entities. Keep in mind that the first returned block will likely be the given block parameter.
	 *
	 * @param block The block to start the iterator from.
	 * @param face The direction at which the iterator should iterate.
	 * @param range The distance the iterator should iterate.
	 * @return Returns a new instance of an BlockIterator.
	 */
	public static BlockIterator getBlockIterator(final Block block, final BlockFace face, final int range) {
		if (!isValidBlock(block)) {
			throw new IllegalArgumentException("Cannot create a block iterator from a null block.");
		}
		if (face == null || face == BlockFace.SELF) {
			throw new IllegalArgumentException("Block iterator requires a valid direction.");
		}
		if (range <= 0) {
			throw new IllegalArgumentException("Block iterator requires a range of 1 or higher.");
		}
		return new BlockIterator(block.getWorld(), block.getLocation().toVector(), face.getDirection(), 0, range);
	}

	/**
	 * Determines whether a given block is liquid source.
	 *
	 * @param block The block to check.
	 * @return Returns true if the block is a liquid source.
	 */
	public static boolean isSourceBlock(final Block block) {
		return block != null
				&& MoreTags.LIQUID_BLOCKS.isTagged(block.getType())
				&& ((Levelled) block.getBlockData()).getLevel() == 0;
	}

}
