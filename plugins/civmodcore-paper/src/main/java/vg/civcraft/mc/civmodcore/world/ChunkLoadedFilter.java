package vg.civcraft.mc.civmodcore.world;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.server.v1_16_R1.BlockPosition;
import org.bukkit.World;
import vg.civcraft.mc.civmodcore.api.WorldAPI;

/**
 * Utility to use with {@link java.util.stream.Stream} to efficiently remove elements from unloaded chunks.
 */
public class ChunkLoadedFilter {

	/**
	 * Creates a new filter function for a given world to remove elements representing blocks in unloaded chunks.
	 *
	 * @param world The world to filter.
	 * @return Returns a new filter function.
	 */
	public static Predicate<BlockPosition> create(final World world) {
		Preconditions.checkArgument(WorldAPI.isWorldLoaded(world));
		final List<Long> loadedChunks = new ArrayList<>();
		return (position) -> {
			if (position == null) {
				return false;
			}
			final int chunkX = position.getX() >> 4;
			final int chunkZ = position.getZ() >> 4;
			final long combined = ((long) chunkX << 32) | (long) chunkZ;
			if (loadedChunks.contains(combined)) {
				return true;
			}
			if (world.isChunkLoaded(chunkX, chunkZ)) {
				loadedChunks.add(combined);
				return true;
			}
			return false;
		};
	}

}
