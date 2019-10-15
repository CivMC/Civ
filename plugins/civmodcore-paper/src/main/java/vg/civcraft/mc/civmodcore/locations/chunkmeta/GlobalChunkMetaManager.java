package vg.civcraft.mc.civmodcore.locations.chunkmeta;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import vg.civcraft.mc.civmodcore.CivModCorePlugin;

public class GlobalChunkMetaManager {

	private Map<UUID, Short> uuidToInternalID;
	private Map<Short, UUID> internalIDToUuid;
	private Map<UUID, WorldChunkMetaManager> worldToManager;
	private ChunkDAO chunkDao;

	public GlobalChunkMetaManager(ChunkDAO chunkDao) {
		this.uuidToInternalID = new TreeMap<>();
		this.worldToManager = new TreeMap<>();
		this.internalIDToUuid = new TreeMap<>();
		this.chunkDao = chunkDao;
		for (World world : Bukkit.getWorlds()) {
			registerWorld(world);
		}
		Bukkit.getPluginManager().registerEvents(new ChunkMetaListener(this), CivModCorePlugin.getInstance());
		Bukkit.getScheduler().scheduleSyncDelayedTask(CivModCorePlugin.getInstance(), () -> {
			for (World world : Bukkit.getWorlds()) {
				for (Chunk chunk : world.getLoadedChunks()) {
					loadChunkData(chunk);
				}
			}
		}, 1L);
	}

	/**
	 * Attempts to retrieve a meta for the given parameter. If none exists yet, the
	 * given lambda will be used to create a new one, insert it and return it
	 * 
	 * @param pluginID Internal id of the plugin the meta belongs to
	 * @param world    World the chunk is in
	 * @param chunkX   X-coord of the chunk
	 * @param chunkZ   Z-coord of the chunk
	 * @param computer Lambda supplying the new ChunkMeta to insert if none exists
	 *                 yet. May not produce null results
	 * @return ChunkMeta for the given parameter, guaranteed not null as long as the
	 *         supplier lambda is valid
	 */
	public ChunkMeta<?> computeIfAbsent(int pluginID, World world, int chunkX, int chunkZ,
			Supplier<ChunkMeta<?>> computer) {
		return getWorldManager(world).computeIfAbsent(pluginID, chunkX, chunkZ, computer);
	}

	/**
	 * Saves all data out to the database
	 */
	public void flushAll() {
		for (WorldChunkMetaManager man : worldToManager.values()) {
			man.flushAll();
		}
	}

	public ChunkDAO getChunkDAO() {
		return chunkDao;
	}

	/**
	 * Retrieves ChunkMeta for the given plugin from the given chunk in the given
	 * world. May be null if no such meta is specified yet
	 * 
	 * @param pluginID Internal id of the plugin the meta belongs to
	 * @param world    World the chunk is in
	 * @param chunkX   X-coord of the chunk
	 * @param chunkZ   Z-coord of the chunk
	 * @return Retrieved ChunkMeta for the given parameter, possibly null
	 */
	public ChunkMeta<?> getChunkMeta(int pluginID, World world, int chunkX, int chunkZ) {
		return getWorldManager(world).getChunkMeta(pluginID, chunkX, chunkZ);
	}

	private WorldChunkMetaManager getWorldManager(World world) {
		return worldToManager.get(world.getUID());
	}

	/**
	 * Inserts ChunkMeta for the given plugin into the given chunk in the given
	 * world. Will silently overwrite any existing data
	 * 
	 * @param pluginID Internal id of the plugin the meta belongs to
	 * @param world    World the chunk is in
	 * @param chunkX   X-coord of the chunk
	 * @param chunkZ   Z-coord of the chunk
	 */
	public void insertChunkMeta(int pluginID, World world, int chunkX, int chunkZ, ChunkMeta<?> meta) {
		meta.setPluginID(pluginID);
		getWorldManager(world).insertChunkMeta(chunkX, chunkZ, meta);
	}

	void loadChunkData(Chunk chunk) {
		WorldChunkMetaManager worldManager = worldToManager.get(chunk.getWorld().getUID());
		if (worldManager == null) {
			throw new IllegalStateException("No world manager for chunk at " + chunk.toString());
		}
		worldManager.loadChunk(chunk.getX(), chunk.getZ());
	}

	/**
	 * Registers a world for internal use
	 * 
	 * @param world World to prepare data structures for
	 * @return Whether successfull or not
	 */
	boolean registerWorld(World world) {
		if (uuidToInternalID.containsKey(world.getUID())) {
			return true;
		}
		short id = chunkDao.getOrCreateWorldID(world);
		if (id == -1) {
			// very bad
			return false;
		}
		uuidToInternalID.put(world.getUID(), id);
		internalIDToUuid.put(id, world.getUID());
		WorldChunkMetaManager manager = new WorldChunkMetaManager(world, id);
		worldToManager.put(world.getUID(), manager);
		return true;
	}

	/**
	 * Registers all currently loaded worlds internally
	 * 
	 * @return Whether all worlds were successfully loaded in or not. Errors here
	 *         would most likely mean a non-working database setup
	 */
	public boolean setup() {
		for (World world : Bukkit.getWorlds()) {
			boolean worked = registerWorld(world);
			if (!worked) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Gets the world object mapped to an internal id
	 * @param id ID to get world for
	 * @return World if a matching one for the given id exists and the world is loaded currently
	 */
	public World getWorldByInternalID(short id) {
		UUID uuid = internalIDToUuid.get(id);
		if (uuid == null) {
			return null;
		}
		return Bukkit.getWorld(uuid);
	}

	void unloadChunkData(Chunk chunk) {
		WorldChunkMetaManager worldManager = worldToManager.get(chunk.getWorld().getUID());
		if (worldManager == null) {
			throw new IllegalStateException("No world manager for chunk at " + chunk.toString());
		}
		worldManager.unloadChunk(chunk.getX(), chunk.getZ());
	}

}
