package vg.civcraft.mc.civmodcore.world.locations.chunkmeta;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.bukkit.World;

/**
 * Stores Chunk metadata for all plugins for one specific world. Metadata is
 * kept in a cache, into which is inserted when a chunk is loaded. When a chunk
 * is unloaded, it is placed in an unloading queue and will be unloaded after a
 * certain interval if it hasn't been reloaded in the mean time
 *
 */
public class WorldChunkMetaManager {

	/**
	 * How long should chunk data be kept in memory after the chunk is unloaded? 5
	 * minutes
	 */
	private static final long UNLOAD_DELAY = 5L * 60L * 1000L;
	private static final long UNLOAD_CHECK_INTERVAL = 1000L;
	
	private static final long REGULAR_SAVE_INTERVAL = 60L * 1000L;

	private final short worldID;
	private final Map<ChunkCoord, ChunkCoord> metas;
	/**
	 * A synchronized TreeSet holding all chunk metadata belonging to unloaded
	 * chunks. A comparator based on when the chunk was unloaded is used to
	 * guarantee an iteration order ascending based on unloading time, which makes
	 * cleanup trivial
	 */
	private final Set<ChunkCoord> unloadingQueue;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
	private final List<AtomicBoolean> chunkLoadingDisablers;
	private final List<Thread> chunkLoadingThreads;
	private final LinkedBlockingQueue<ChunkCoord> chunkLoadingQueue;
	private final World world;
	private final Logger logger;

	public WorldChunkMetaManager(World world, short worldID, int chunkLoadingThreadCount, Logger logger) {
		this.worldID = worldID;
		this.world = world;
		this.metas = new HashMap<>();
		this.unloadingQueue = Collections.synchronizedSet(new TreeSet<>((a, b) -> {
			int timeDiff = Math.toIntExact(a.getLastMCUnloadingTime() - b.getLastMCUnloadingTime());
			if (timeDiff != 0) {
				return timeDiff;
			}
			return a.compareTo(b);
		}));

		this.chunkLoadingQueue = new LinkedBlockingQueue<>();
		this.chunkLoadingDisablers = new ArrayList<>();
		this.chunkLoadingThreads = new ArrayList<>();
		this.logger = logger;

		registerUnloadRunnable();
		startChunkLoadingThreads(chunkLoadingThreadCount);
		registerRegularSaveRunnable();
	}

	ChunkMeta<?> computeIfAbsent(short pluginID, int x, int z, Supplier<ChunkMeta<?>> computer, boolean alwaysLoaded) {
		ChunkCoord coord = getChunkCoord(x, z, true, false);
		ChunkMeta<?> existing = coord.getMeta(pluginID, alwaysLoaded);
		if (existing != null) {
			return existing;
		}
		existing = computer.get();
		existing.setChunkCoord(coord);
		existing.setPluginID(pluginID);
		coord.addChunkMeta(existing);
		return existing;
	}

	void flushPluginData(short pluginID) {
		synchronized (metas) {
			for (ChunkCoord coord : metas.keySet()) {
				synchronized (coord) {
					coord.persistPlugin(pluginID);
				}
			}
		}
	}

	/**
	 * Retrieves or generates a new ChunkCoord instance. ChunkCoord are each
	 * singletons for their location, which is enforced through this method
	 * 
	 * @param x   X-coordinate of the chunk
	 * @param z   Z-coordinate of the chunk
	 * @param gen Should a new ChunkCoord be generated if none exists at the given
	 *            location
	 * @param gen Should data for the ChunkCoord be loaded from the database if a
	 *            new one was created
	 * @return Found/Generated ChunkCoord or null if none existed and none was
	 *         supposed to be generated
	 */
	private ChunkCoord getChunkCoord(int x, int z, boolean gen, boolean populate) {
		ChunkCoord coord = new ChunkCoord(x, z, worldID, world);
		synchronized (metas) {
			ChunkCoord value = metas.get(coord);
			if (value != null) {
				return value;
			}
			if (!gen) {
				return null;
			}
			metas.put(coord, coord);
			if (populate) {
				// up until here we are still sync from the ChunkLoadEvent, so we need to
				// offload the actual db load to another thread
				synchronized (chunkLoadingQueue) {
					chunkLoadingQueue.add(coord);
					chunkLoadingQueue.notifyAll();
				}
			}
			return coord;
		}
	}

	/**
	 * Retrieves the chunk meta for a specific chunk for a specific plugin.
	 * If it is not loaded then just return and do not wait
	 *
	 * DO NOT USE THIS FOR UNLOADED CHUNKS, THINGS WILL BREAK HORRIBLY
	 *
	 * @param pluginID Internal id of the plugin
	 * @param x        X-coordinate of the chunk
	 * @param z        Z-coordinate of the chunk
	 * @return ChunkMetaLoadStatus for the given parameter
	 */
	ChunkMetaLoadStatus getChunkMetaIfLoaded(short pluginID, int x, int z, boolean alwaysLoaded) {
		ChunkCoord coord = getChunkCoord(x, z, false, false);
		if (coord == null) {
			return null;
		}
		return coord.getMetaIfLoaded(pluginID, alwaysLoaded);
	}

	/**
	 * Retrieves the chunk meta for a specific chunk for a specific plugin.
	 * 
	 * DO NOT USE THIS FOR UNLOADED CHUNKS, THINGS WILL BREAK HORRIBLY
	 * 
	 * @param pluginID Internal id of the plugin
	 * @param x        X-coordinate of the chunk
	 * @param z        Z-coordinate of the chunk
	 * @return ChunkMeta for the given parameter, possibly null if none existed
	 */
	ChunkMeta<?> getChunkMeta(short pluginID, int x, int z, boolean alwaysLoaded) {
		ChunkCoord coord = getChunkCoord(x, z, false, false);
		if (coord == null) {
			return null;
		}
		return coord.getMeta(pluginID, alwaysLoaded);
	}

	/**
	 * Inserts new chunk metadata, overwriting any existing one for the same plugin
	 * and the same chunk
	 * 
	 * @param x    X-coordinate of the chunk
	 * @param z    Z-coordinate of the chunk
	 * @param meta Metadata to insert
	 */
	void insertChunkMeta(int x, int z, ChunkMeta<?> meta) {
		ChunkCoord coord = getChunkCoord(x, z, true, false);
		meta.setChunkCoord(coord);
		coord.addChunkMeta(meta);
	}

	/**
	 * Called when the underlying minecraft chunk is loaded. Loads the chunk
	 * metadata from the database if its not already available in the cache
	 * 
	 * @param x X-coordinate of the chunk
	 * @param z Z-coordinate of the chunk
	 */
	void loadChunk(int x, int z) {
		ChunkCoord chunkCoord = getChunkCoord(x, z, true, true);
		if (chunkCoord.getLastMCUnloadingTime() != -1) {
			unloadingQueue.remove(chunkCoord);
		}
		chunkCoord.minecraftChunkLoaded();
	}
	
	private void registerRegularSaveRunnable() {
		scheduler.scheduleWithFixedDelay(() -> {
			saveAllChunks();
		}, REGULAR_SAVE_INTERVAL, REGULAR_SAVE_INTERVAL, TimeUnit.MILLISECONDS);
	}

	private void saveAllChunks() {
		//we don't take a lock on metas, because we will not modify it
		for(ChunkCoord coord : metas.values()) {
			synchronized (coord) {
				if (!coord.isChunkLoaded()) {
					// to avoid race conditions, we will not write out chunks currently unloaded
					continue;
				}
				coord.fullyPersist();
			}
		}
	}

	private void registerUnloadRunnable() {
		scheduler.scheduleWithFixedDelay(() -> {
			if (unloadingQueue.isEmpty()) {
				return;
			}
			long currentTime = System.currentTimeMillis();
			synchronized (unloadingQueue) {
				Iterator<ChunkCoord> iter = unloadingQueue.iterator();
				while (iter.hasNext()) {
					ChunkCoord coord = iter.next();
					// Is time up?
					if (currentTime - coord.getLastMCUnloadingTime() > UNLOAD_DELAY) {
						// make sure chunk hasnt loaded again since
						if (coord.getLastMCUnloadingTime() > coord.getLastMCLoadingTime()) {
							synchronized (metas) {
								synchronized (coord) {
									coord.fullyPersist();
									iter.remove();
									if (!coord.hasPermanentlyLoadedData()) {
										metas.remove(coord);
										// coord is up for garbage collection at this point and all of its data has been
										// written to the db
									} else {
										coord.deleteNonPersistentData();
										// keep chunk coord, but garbage collect the data we dont want to keep inside of
										// it
									}
								}
							}
						}
						else {
							//chunk was loaded again, remove it from unloading queue
							iter.remove();
						}
					} else {
						// tree set iterator is guaranteed to be in ascending order and we use the
						// timestamp of unloading as key,
						// so any subsequent chunks will also have been unloaded for less time
						break;
					}
				}
			}

		}, UNLOAD_CHECK_INTERVAL, UNLOAD_CHECK_INTERVAL, TimeUnit.MILLISECONDS);
	}

	private void startChunkLoadingThreads(int chunkLoadingCount) {
		for (int i = 0; i < chunkLoadingCount; i++) {
			final int threadIndex = i;

			final AtomicBoolean disabled = new AtomicBoolean(false);
			this.chunkLoadingDisablers.add(disabled);

			final String threadName = "cmc-chunk-loading-" + this.worldID + "-" + i;
			Thread thread = new Thread(() -> chunkLoadingThread(threadIndex, threadName, disabled), threadName);
			this.chunkLoadingThreads.add(thread);
			thread.start();
		}
	}

	private void chunkLoadingThread(int threadIndex, String threadName, AtomicBoolean disabled) {
		this.logger.info("[" + this.world.getName() + "] Thread " + threadName + " is started.");

		while (!disabled.get()) {
			try {
				ChunkCoord coord = chunkLoadingQueue.take();
				coord.loadAll(threadIndex);
			} catch (InterruptedException e) {
				if(!disabled.get()) e.printStackTrace();
			}
		}

		this.logger.info("[" + this.world.getName() + "] Thread " + threadName + " is stopped.");
	}

	/**
	 * Called when the underlying minecraft chunk is unloaded. Does not actually
	 * unload our data, but instead stages it to be unloaded if the chunk stays
	 * unloaded for a certain period of time
	 * 
	 * @param x X-coordinate of the chunk
	 * @param z Z-coordinate of the chunk
	 */
	void unloadChunk(int x, int z) {
		ChunkCoord chunkCoord = getChunkCoord(x, z, false, false);
		// chunkCoord can never be null here, otherwise our data structure would be
		// broken, in which case we'd want to know
		chunkCoord.minecraftChunkUnloaded();
		unloadingQueue.add(chunkCoord);
	}

	public void disable() {
		for (int i = 0; i < this.chunkLoadingDisablers.size(); i++) {
			AtomicBoolean disabled = this.chunkLoadingDisablers.get(i);
			disabled.set(true);

			Thread chunkLoadingThread = this.chunkLoadingThreads.get(i);
			chunkLoadingThread.interrupt();
		}

		this.scheduler.shutdown();

		try {
			if (!this.scheduler.awaitTermination(60, TimeUnit.SECONDS))
				this.scheduler.shutdownNow();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

		this.logger.info("[" + this.world.getName() + "] Scheduler and its tasks are shutdown.");

		saveAllChunks();

		this.logger.info("[" + this.world.getName() + "] All chunks have been saved.");
	}
}
