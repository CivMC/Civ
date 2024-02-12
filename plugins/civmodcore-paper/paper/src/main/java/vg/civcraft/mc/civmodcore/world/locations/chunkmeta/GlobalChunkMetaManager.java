package vg.civcraft.mc.civmodcore.world.locations.chunkmeta;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.api.ChunkMetaViewTracker;
import vg.civcraft.mc.civmodcore.world.locations.global.CMCWorldDAO;
import vg.civcraft.mc.civmodcore.world.locations.global.WorldIDManager;

public class GlobalChunkMetaManager {
	private final CMCWorldDAO chunkDao;
	private final Map<UUID, WorldChunkMetaManager> worldToManager;
	private final int chunkLoadingThreadCount;
	private final Logger logger;

	public int getChunkLoadingThreadCount() {
		return this.chunkLoadingThreadCount;
	}

	public GlobalChunkMetaManager(CMCWorldDAO chunkDao, WorldIDManager idManager, int chunkLoadingThreadCount) {
		this.chunkDao = chunkDao;
		this.worldToManager = new TreeMap<>();
		this.chunkLoadingThreadCount = chunkLoadingThreadCount;
		this.logger = CivModCorePlugin.getInstance().getLogger();

		for (World world : Bukkit.getWorlds()) {
			registerWorld(idManager.getInternalWorldId(world), world);
		}
		Bukkit.getPluginManager().registerEvents(new ChunkMetaListener(this, ChunkMetaViewTracker.getInstance()), CivModCorePlugin.getInstance());
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
	public ChunkMeta<?> computeIfAbsent(short pluginID, World world, int chunkX, int chunkZ,
			Supplier<ChunkMeta<?>> computer, boolean alwaysLoaded) {
		return getWorldManager(world).computeIfAbsent(pluginID, chunkX, chunkZ, computer, alwaysLoaded);
	}
	
	/**
	 * Saves all data for one specific plugin out to the database
	 * @param pluginID Internal id of the plugin to save data for
	 */
	public void flushPlugin(short pluginID) {
		for (WorldChunkMetaManager man : worldToManager.values()) {
			man.flushPluginData(pluginID);
		}
	}

	public CMCWorldDAO getChunkDAO() {
		return chunkDao;
	}

	/**
	 * Retrieves ChunkMeta for the given plugin from the given chunk in the given
	 * world.
	 * If it is not loaded then just return and do not wait
	 *
	 * @param pluginID Internal id of the plugin the meta belongs to
	 * @param world    World the chunk is in
	 * @param chunkX   X-coord of the chunk
	 * @param chunkZ   Z-coord of the chunk
	 * @return Retrieved ChunkMetaLoadStatus for the given parameter
	 */
	public ChunkMetaLoadStatus getChunkMetaIfLoaded(short pluginID, World world, int chunkX, int chunkZ, boolean alwaysLoaded) {
		return getWorldManager(world).getChunkMetaIfLoaded(pluginID, chunkX, chunkZ, alwaysLoaded);
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
	public ChunkMeta<?> getChunkMeta(short pluginID, World world, int chunkX, int chunkZ, boolean alwaysLoaded) {
		return getWorldManager(world).getChunkMeta(pluginID, chunkX, chunkZ, alwaysLoaded);
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
	public void insertChunkMeta(short pluginID, World world, int chunkX, int chunkZ, ChunkMeta<?> meta) {
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

	void unloadChunkData(Chunk chunk) {
		WorldChunkMetaManager worldManager = worldToManager.get(chunk.getWorld().getUID());
		if (worldManager == null) {
			throw new IllegalStateException("No world manager for chunk at " + chunk.toString());
		}
		worldManager.unloadChunk(chunk.getX(), chunk.getZ());
	}
	
	public void registerWorld(short id, World world) {
		WorldChunkMetaManager manager = new WorldChunkMetaManager(world, id, this.chunkLoadingThreadCount, this.logger);
		worldToManager.put(world.getUID(), manager);
	}

	public void disableWorlds() {
		for (WorldChunkMetaManager manager : worldToManager.values()) {
			manager.disable();
		}
	}
}
