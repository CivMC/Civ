package vg.civcraft.mc.civmodcore.locations.chunkmeta.api;

import java.util.function.Supplier;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civmodcore.locations.chunkmeta.ChunkMeta;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.GlobalChunkMetaManager;

public class ChunkMetaView<T extends ChunkMeta<?>> {

	protected int pluginID;
	protected JavaPlugin plugin;
	protected GlobalChunkMetaManager globalManager;

	ChunkMetaView(JavaPlugin plugin, int pluginID, GlobalChunkMetaManager globalManager) {
		this.plugin = plugin;
		this.pluginID = pluginID;
		this.globalManager = globalManager;
	}

	/**
	 * Attempts to retrieve metadata for the given chunk. If none exists yet, the
	 * given lambda will be used to create a new one, insert it and return it
	 * 
	 * @param chunk    Chunk to get/insert metadata for
	 * @param computer Lambda supplying the new ChunkMeta to insert if none exists
	 *                 yet. May not produce null results
	 * @return ChunkMeta for the given parameter, guaranteed not null as long as the
	 *         supplier lambda is valid
	 */
	public T computeIfAbsent(Chunk chunk, Supplier<ChunkMeta<?>> computer) {
		return computeIfAbsent(chunk.getWorld(), chunk.getX(), chunk.getZ(), computer);
	}

	/**
	 * Attempts to retrieve metadata for the chunk at the given chunk coords in the
	 * given world. If none exists yet, the given lambda will be used to create a
	 * new one, insert it and return it
	 * 
	 * @param world    World the chunk is in
	 * @param chunkX   X-coord of the chunk
	 * @param chunkZ   Z-coord of the chunk
	 * @param computer Lambda supplying the new ChunkMeta to insert if none exists
	 *                 yet. May not produce null results
	 * @return ChunkMeta for the given parameter, guaranteed not null as long as the
	 *         supplier lambda is valid
	 */
	@SuppressWarnings("unchecked")
	public T computeIfAbsent(World world, int chunkX, int chunkZ, Supplier<ChunkMeta<?>> computer) {
		if (globalManager == null) {
			throw new IllegalStateException("View already shut down, can not read data");
		}
		if (!world.getChunkAt(chunkX, chunkZ).isLoaded()) {
			throw new IllegalArgumentException("Can not insert meta for unloaded chunks");
		}
		return (T) globalManager.computeIfAbsent(pluginID, world, chunkX, chunkZ, computer);
	}

	/**
	 * Shuts down this instance, saving out all of its data to the database. No
	 * calls to this instance should be made during or after this call, create a new
	 * instance instead if neccessary
	 */
	public void disable() {
		if (globalManager == null) {
			// already shut down
			return;
		}
		GlobalChunkMetaManager globalTemp = globalManager;
		globalManager = null;
		globalTemp.flushAll();
		ChunkMetaAPI.removePlugin(plugin);
	}

	/**
	 * Retrieves chunk metadata for the given chunk with the given for this specific
	 * plugin
	 * 
	 * @param chunk Chunk to get metadata for
	 * @return ChunkMeta for the requested chunk owned by this plugin, possibly null
	 *         if no such data exists yet
	 */
	public T getChunkMeta(Chunk chunk) {
		if (chunk == null) {
			throw new IllegalArgumentException("Chunk may not be null");
		}
		return getChunkMeta(chunk.getWorld(), chunk.getX(), chunk.getZ());
	}

	/**
	 * Retrieves chunk metadata for the given chunk with the given for this specific
	 * plugin
	 * 
	 * @param chunk Chunk to get metadata for
	 * @return ChunkMeta for the requested chunk owned by this plugin, possibly null
	 *         if no such data exists yet
	 */
	public T getChunkMeta(Location location) {
		if (location == null) {
			throw new IllegalArgumentException("Location may not be null");
		}
		return getChunkMeta(location.getWorld(), location.getChunk().getX(), location.getChunk().getZ());
	}

	/**
	 * Retrieves chunk metadata in the given world for the chunk with the given
	 * chunk coordinates for this specific plugin
	 * 
	 * @param world  World the chunk is in
	 * @param chunkX X-Coordinate of the chunk
	 * @param chunkZ Z-Coordinate of the chunk
	 * @return ChunkMeta for the requested chunk owned by this plugin, possibly null
	 *         if no such data exists yet
	 */
	@SuppressWarnings("unchecked")
	public T getChunkMeta(World world, int chunkX, int chunkZ) {
		if (world == null) {
			throw new IllegalArgumentException("World may not be null");
		}
		if (globalManager == null) {
			throw new IllegalStateException("View already shut down, can not read data");
		}
		return (T) globalManager.getChunkMeta(pluginID, world, chunkX, chunkZ);
	}

	/**
	 * Inserts metadata tied to the given chunk. Will silently overwrite any
	 * existing data
	 * 
	 * @param chunk Chunk metadata belongs to
	 * @param meta  Metadata to insert
	 */
	public void insertChunkMeta(Chunk chunk, T meta) {
		if (chunk == null) {
			throw new IllegalArgumentException("Chunk may not be null");
		}
		insertChunkMeta(chunk.getWorld(), chunk.getX(), chunk.getZ(), meta);
	}

	/**
	 * Inserts metadata tied to the given chunk coords in the given world. Will
	 * silently overwrite any existing data
	 * 
	 * @param world  World the chunk is in
	 * @param chunkX X-coord of the chunk
	 * @param chunkZ Z-coord of the chunk
	 * @param meta   Metadata to insert
	 */
	public void insertChunkMeta(World world, int chunkX, int chunkZ, T meta) {
		if (meta == null) {
			throw new IllegalArgumentException("Meta may not be null");
		}
		if (world == null) {
			throw new IllegalArgumentException("World may not be null");
		}
		if (globalManager == null) {
			throw new IllegalStateException("View already shut down, can not read data");
		}
		if (!world.getChunkAt(chunkX, chunkZ).isLoaded()) {
			throw new IllegalArgumentException("Can not insert meta for unloaded chunks");
		}
		globalManager.insertChunkMeta(pluginID, world, chunkX, chunkZ, meta);
	}

}
