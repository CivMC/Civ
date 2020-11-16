package vg.civcraft.mc.civmodcore.world;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import vg.civcraft.mc.civmodcore.api.LocationAPI;

/**
 * This tracker should provide an efficient way of only using chunks that are loaded in.
 */
public class ChunkTracker implements Listener {

	private static final Map<WorldXZ, Chunk> STORAGE = new HashMap<>();

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChunkLoad(final ChunkLoadEvent event) {
		final Chunk chunk = event.getChunk();
		STORAGE.put(new WorldXZ(chunk.getWorld(), chunk.getX(), chunk.getZ()), chunk);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChunkUnload(final ChunkUnloadEvent event) {
		final Chunk chunk = event.getChunk();
		STORAGE.remove(new WorldXZ(chunk.getWorld(), chunk.getX(), chunk.getZ()));
	}

	/**
	 * Retrieves the {@link Chunk} object for the given chunk coordinate.
	 *
	 * @param location The location to get the chunk of. MUST NOT BE A BLOCK LOCATION!
	 * @return Returns the chunk's instance or null if it's not loaded.
	 */
	public static Chunk getLoadedChunk(final Location location) {
		if (!LocationAPI.isValidLocation(location)) {
			return null;
		}
		return STORAGE.get(new WorldXZ(location));
	}

	/**
	 * Resets the tracker. Should only be used when disabling CivModCore.
	 */
	public static void reset() {
		STORAGE.clear();
	}

}
