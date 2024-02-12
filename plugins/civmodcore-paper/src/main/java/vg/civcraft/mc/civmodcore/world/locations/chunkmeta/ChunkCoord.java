package vg.civcraft.mc.civmodcore.world.locations.chunkmeta;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.bukkit.World;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.api.ChunkMetaViewTracker;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.stat.LoadStatisticManager;

public class ChunkCoord extends XZWCoord {
	private static final long  INVALID_TIME = -1;

	/**
	 * When was this chunk last loaded in Minecraft as UNIX timestamp
	 */
	private long lastLoadedTime;
	/**
	 * When was this chunk last unloaded in Minecraft as UNIX timestamp
	 */
	private long lastUnloadedTime;
	/**
	 * Each ChunkMeta belongs to one plugin, they are identified by the plugin id
	 */
	private final Map<Short, ChunkMeta<?>> chunkMetas;
	/**
	 * Set to true once all data has been loaded for this chunk and stays true for
	 * the entire life time of this object
	 */
	private final AtomicBoolean isFullyLoaded = new AtomicBoolean(false);
	private final World world;

	ChunkCoord(int x, int z, short worldID, World world) {
		super(x, z, worldID);
		this.world = world;
		this.chunkMetas = new TreeMap<>();
		this.lastLoadedTime = INVALID_TIME;
		this.lastUnloadedTime = INVALID_TIME;
	}

	/**
	 * @return World this instance is in
	 */
	public World getWorld() {
		return world;
	}

	void addChunkMeta(ChunkMeta<?> chunkMeta) {
		chunkMeta.setWorld(this.world);
		chunkMetas.put(chunkMeta.getPluginID(), chunkMeta);
	}

	/**
	 * Writes all data held by this instance to the database
	 */
	void fullyPersist() {
		for (ChunkMeta<?> chunkMeta : chunkMetas.values()) {
			persistChunkMeta(chunkMeta);
		}
	}

	/**
	 * Writes all data held by this instance for one specific plugin to the database
	 *
	 * @param id Internal id of the plugin to save data for
	 */
	void persistPlugin(short id) {
		ChunkMeta<?> chunkMeta = chunkMetas.get(id);
		if (chunkMeta != null) {
			persistChunkMeta(chunkMeta);
		}
	}

	private static void persistChunkMeta(ChunkMeta<?> chunkMeta) {
		switch (chunkMeta.getCacheState()) {
			case NORMAL:
				break;
			case MODIFIED:
				chunkMeta.update();
				break;
			case NEW:
				chunkMeta.insert();
				break;
			case DELETED:
				chunkMeta.delete();
				break;
			default:
				throw new IllegalArgumentException("Unsupported cache state '" + chunkMeta.getCacheState() + "'");
		}
		chunkMeta.setCacheState(CacheState.NORMAL);
	}

	/**
	 * Forget all data which is not supposed to be held in memory permanently
	 */
	void deleteNonPersistentData() {
		Iterator<Entry<Short, ChunkMeta<?>>> iter = chunkMetas.entrySet().iterator();
		while (iter.hasNext()) {
			ChunkMeta<?> meta = iter.next().getValue();
			if (!meta.loadAlways()) {
				iter.remove();
			}
		}
	}

	/**
	 * @return When was the minecraft chunk (the block data) this object is tied
	 * last unloaded (UNIX timestamp)
	 */
	long getLastUnloadedTime() {
		return lastUnloadedTime;
	}

	void clearUnloaded() {
		lastUnloadedTime = INVALID_TIME;
	}

	boolean isUnloaded() {
		return lastUnloadedTime > lastLoadedTime;
	}

	ChunkMetaLoadStatus getMetaIfLoaded(short pluginID, boolean alwaysLoaded) {
		if (!alwaysLoaded && !isFullyLoaded.get())
			return new ChunkMetaLoadStatus(null, false);

		ChunkMeta<?> meta = getMeta(pluginID, alwaysLoaded);

		return new ChunkMetaLoadStatus(meta, true);
	}

	ChunkMeta<?> getMeta(short pluginID, boolean alwaysLoaded) {
		// This call is cheap and should be done in any case. Threads will be parked when necessary on relevant code
		// sections.
		if (!alwaysLoaded)
			loadAll(LoadStatisticManager.MainThreadIndex);

		return chunkMetas.get(pluginID);
	}

	boolean hasPermanentlyLoadedData() {
		for (ChunkMeta<?> meta : chunkMetas.values()) {
			if (meta.loadAlways()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Loads data for all plugins for this chunk
	 *
	 * @param threadIndex Specifies the index of the thread used to load this chunk
	 *                    It is literally index of the Thread in the list WorldChunkMetaManager::chunkLoadingThreads
	 *                    Used here just as informative field provided to statistics polling
	 *                    Doesn't not influence any mechanics
	 *
	 *                    If threadIndex == LoadStatisticManager.MainThreadIndex then this means
	 *                    that the "main thread" called this function
	 *
	 *                    If the server has enough resources to perform requests then "main thread"
	 *                    will come to loadAll() always at the time when either chunk is loaded (the best case)
	 *                    or when it is loading now by the thread from WorldChunkMetaManager
	 */
	void loadAll(int threadIndex) {
		// Skip the monitor check if this is set to true.
		if (isFullyLoaded.get()) return;
		// Lets to an expensive synchronization here if necessary.
		synchronized (this) {
			if (!isFullyLoaded.get()) {
				for (ChunkMetaInitializer initializer : ChunkMetaFactory.getInstance().getInitializers())
					loadPluginChunk(threadIndex, initializer);

				isFullyLoaded.set(true);
			}
		}
	}

	void loadPluginChunk(int threadIndex, ChunkMetaInitializer initializer) {
		LoadStatisticManager.start(this.world, threadIndex, initializer.pluginId);

		ChunkMeta<?> chunk = initializer.generator.get();
		short pluginId = initializer.pluginId;

		chunk.setChunkCoord(this);
		chunk.setPluginID(pluginId);

		try {
			chunk.populate();
		} catch (Throwable e) {
			CivModCorePlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to load chunk data", e);
		}

		ChunkMetaViewTracker.getInstance().get(pluginId).postLoad(chunk);
		addChunkMeta(chunk);

		LoadStatisticManager.stop(this.world, threadIndex, initializer.pluginId);
	}

	/**
	 * Called when the minecraft chunk (the block data) this object is tied to gets
	 * loaded
	 */
	void minecraftChunkLoaded() {
		boolean hasBeenLoadedBefore = this.lastLoadedTime != INVALID_TIME;
		this.lastLoadedTime = System.currentTimeMillis();
		if (hasBeenLoadedBefore) {
			for (ChunkMeta<?> meta : chunkMetas.values()) {
				try {
					meta.handleChunkCacheReuse();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public boolean isChunkLoaded() {
		if (this.lastUnloadedTime > 0) {
			return this.lastUnloadedTime < this.lastLoadedTime;
		} else {
			return this.lastLoadedTime > 0;
		}
	}

	/**
	 * Called when the minecraft chunk (the block data) this object is tied to gets
	 * unloaded
	 */
	void minecraftChunkUnloaded() {
		this.lastUnloadedTime = System.currentTimeMillis();
		for (ChunkMeta<?> meta : chunkMetas.values()) {
			try {
				meta.handleChunkUnload();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
