package vg.civcraft.mc.civmodcore.api;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

/**
 * Class of utility functions for Worlds.
 *
 * @deprecated Use {@link WorldUtils} instead.
 */
@Deprecated
public final class WorldAPI {

	/**
	 * Determines if a world is currently loaded.
	 *
	 * @param world World to test.
	 * @return Returns true if the world is loaded.
	 *
	 * @deprecated Use {@link WorldUtils#isWorldLoaded(World)} instead.
	 */
	@Deprecated
	public static boolean isWorldLoaded(World world) {
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
	 *
	 * @deprecated Use {@link WorldUtils#isChunkLoaded(World, int, int)} instead.
	 */
	@Deprecated
	public static boolean isChunkLoaded(World world, int x, int z) {
		if (!isWorldLoaded(world)) {
			return false;
		}
		return world.isChunkLoaded(x, z);
	}

	/**
	 * Retrieves a chunk only if it's loaded.
	 *
	 * @param world The world the target chunk is located within.
	 * @param x The (CHUNK) X coordinate.
	 * @param z The (CHUNK) Z coordinate.
	 * @return Returns the loaded chunk, or null.
	 *
	 * @deprecated Use {@link WorldUtils#getLoadedChunk(World, int, int)} instead.
	 */
	@Deprecated
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
	 * @return Returns true if the block is laoded.
	 *
	 * @deprecated Use {@link WorldUtils#isBlockLoaded(Location)} instead.
	 */
	@Deprecated
	public static boolean isBlockLoaded(Location location) {
		if (!LocationAPI.isValidLocation(location)) {
			return false;
		}
		World world = Objects.requireNonNull(location.getWorld());
		int chunkX = location.getBlockX() >> 4;
		int chunkZ = location.getBlockZ() >> 4;
		return world.isChunkLoaded(chunkX, chunkZ);
	}

}
