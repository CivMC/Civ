package vg.civcraft.mc.civmodcore.world.locations.chunkmeta;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.logging.Level;

import org.bukkit.World;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.api.ChunkMetaViewTracker;

public class ChunkCoord extends XZWCoord {

	/**
	 * When was this chunk last loaded in Minecraft as UNIX timestamp
	 */
	private long lastLoadingTime;
	/**
	 * When was this chunk last unloaded in Minecraft as UNIX timestamp
	 */
	private long lastUnloadingTime;
	/**
	 * Each ChunkMeta belongs to one plugin, they are identified by the plugin id
	 */
	private Map<Short, ChunkMeta<?>> chunkMetas;
	/**
	 * Set to true once all data has been loaded for this chunk and stays true for
	 * the entire life time of this object
	 */
	private AtomicBoolean isFullyLoaded = new AtomicBoolean(false);
	private ReentrantLock loadingLock = new ReentrantLock();
	private World world;

	ChunkCoord(int x, int z, short worldID, World world) {
		super(x, z, worldID);
		this.world = world;
		this.chunkMetas = new TreeMap<>();
		this.lastLoadingTime = -1;
		this.lastUnloadingTime = -1;
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
	 * last loaded (UNIX timestamp)
	 */
	long getLastMCLoadingTime() {
		return lastLoadingTime;
	}

	/**
	 * @return When was the minecraft chunk (the block data) this object is tied
	 * last unloaded (UNIX timestamp)
	 */
	long getLastMCUnloadingTime() {
		return lastUnloadingTime;
	}

	ChunkMeta<?> getMeta(short pluginID, boolean alwaysLoaded) {
		// Make this call idempotent
		loadAll();

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
	 */
	void loadAll() {
		// Skip the monitor check if this is set to true.
		if (isFullyLoaded.get()) return;
		// Lets to an expensive synchronization here if necessary.
		synchronized (this) {
			if (!isFullyLoaded.get()) {
				for (Entry<Short, Supplier<ChunkMeta<?>>> generator : ChunkMetaFactory.getInstance()
						.getEmptyChunkFunctions()) {
					ChunkMeta<?> chunk = generator.getValue().get();
					chunk.setChunkCoord(this);
					short pluginID = generator.getKey();
					chunk.setPluginID(pluginID);
					try {
						chunk.populate();
					} catch (Exception e) {
						CivModCorePlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to load chunk data", e);
					}
					ChunkMetaViewTracker.getInstance().get(pluginID).postLoad(chunk);
					addChunkMeta(chunk);
				}
				isFullyLoaded.set(true);
			}
		}
	}

	/**
	 * Called when the minecraft chunk (the block data) this object is tied to gets
	 * loaded
	 */
	void minecraftChunkLoaded() {
		boolean hasBeenLoadedBefore = this.lastLoadingTime != -1;
		this.lastLoadingTime = System.currentTimeMillis();
		if (hasBeenLoadedBefore) {
			for (ChunkMeta<?> meta : chunkMetas.values()) {
				meta.handleChunkCacheReuse();
			}
		}
	}

	public boolean isChunkLoaded() {
		if (this.lastUnloadingTime > 0) {
			return this.lastUnloadingTime < this.lastLoadingTime;
		} else {
			return this.lastLoadingTime > 0;
		}
	}

	/**
	 * Called when the minecraft chunk (the block data) this object is tied to gets
	 * unloaded
	 */
	void minecraftChunkUnloaded() {
		this.lastUnloadingTime = System.currentTimeMillis();
		for (ChunkMeta<?> meta : chunkMetas.values()) {
			meta.handleChunkUnload();
		}
	}

}
