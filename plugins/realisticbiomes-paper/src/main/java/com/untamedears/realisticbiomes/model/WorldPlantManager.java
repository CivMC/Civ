package com.untamedears.realisticbiomes.model;

import java.util.Iterator;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.untamedears.realisticbiomes.RealisticBiomes;

public class WorldPlantManager {

	private final int worldID;
	private final LoadingCache<ChunkCoord, ChunkCache> plantCache;
	private final World world;
	private int schedulerTask;
	private TreeSet<ChunkCache> queueItems;

	public WorldPlantManager(RBDAO db, int worldID, World world) {
		this.worldID = worldID;
		this.world = world;
		this.plantCache = CacheBuilder.newBuilder().removalListener(new RemovalListener<ChunkCoord, ChunkCache>() {
			@Override
			public void onRemoval(RemovalNotification<ChunkCoord, ChunkCache> removal) {
				ChunkCache chunk = removal.getValue();
				if (chunk.isDirty()) {
					db.savePlants(chunk);
				}
			}
		}).build(new CacheLoader<ChunkCoord, ChunkCache>() {
			@Override
			public ChunkCache load(ChunkCoord loc) throws Exception {
				return db.loadPlants(loc, WorldPlantManager.this);
			}
		});
		queueItems = new TreeSet<>((a, b) -> {
			int comp = Long.compare(a.getNextGrowthUpdateTime(), b.getNextGrowthUpdateTime());
			if (comp == 0) {
				return a.getChunkPair().compareTo(b.getChunkPair());
			}
			return comp;
		});
		for (Chunk chunk : world.getLoadedChunks()) {
			loadChunkData(chunk);
		}
		this.schedulerTask = -1;
		rescheduleGrowthUpdate();
	}

	void addToGrowthQueue(ChunkCache cache) {
		System.out.println("Adding cache");
		boolean newFirst;
		if (queueItems.isEmpty()) {
			System.out.println("empty");
			newFirst = true;
		}
		else {
			newFirst = cache.getNextGrowthUpdateTime() < queueItems.iterator().next().getNextGrowthUpdateTime();
			System.out.println(queueItems.iterator().next().getNextGrowthUpdateTime());
		}
		System.out.println(newFirst);
		queueItems.add(cache);
		if (newFirst) {
			rescheduleGrowthUpdate();
		}
	}

	/**
	 * Returns the Plant for the specified block. World is not checked at this stage
	 * 
	 * @param block Location to get plant for
	 * @return Plant at the location if one exists, otherwise null
	 */
	public Plant getPlant(Location loc) {
		if (loc == null) {
			throw new IllegalArgumentException("Can not get plant for null location");
		}
		ChunkCache cache = plantCache.getIfPresent(ChunkCoord.forLocation(loc));
		if (cache == null) {
			throw new IllegalStateException("You can not check plant growth for unloaded chunks. Load the chunk first");
		}
		Plant plant = cache.getPlant(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		if (plant == null) {
			return null;
		}
		return plant;
	}

	public World getWorld() {
		return world;
	}

	/**
	 * @return Internal id of the world for which this instance manages plants
	 */
	public int getWorldID() {
		return worldID;
	}

	public void insertPlant(Plant plant) {
		ChunkCoord key = ChunkCoord.forLocation(plant.getLocation());
		ChunkCache cache = plantCache.getIfPresent(key);
		if (cache == null) {
			throw new IllegalStateException("Chunk for created plant was not loaded");
		}
		cache.insertPlant(plant);
	}

	/**
	 * Used to flush all the plants to the db on shutdown. Can be called else where
	 * too if a manual flush is wanted.
	 */
	public void invalidateAllPlants() {
		plantCache.invalidateAll();
		plantCache.cleanUp();
	}

	/**
	 * Checks if the location has a tracked plant or not.
	 * 
	 * @param loc The location of the plant.
	 * @return True if a tracked plant exists at the given location, false otherwise
	 */
	public boolean isTracked(Location loc) {
		return getPlant(loc) != null;
	}

	void loadChunkData(Chunk chunk) {
		ChunkCache cache;
		try {
			cache = plantCache.get(ChunkCoord.forChunk(chunk));
		} catch (ExecutionException e) {
			RealisticBiomes.getInstance().getLogger().severe("Failed to load chunk data: " + e.toString());
			return;
		}
		queueItems.add(cache);
	}

	void removeFromGrowthQueue(ChunkCache cache) {
		if (queueItems.isEmpty()) {
			return;
		}
		boolean wasFirst = queueItems.iterator().next() == cache;
		queueItems.remove(cache);
		if (wasFirst) {
			rescheduleGrowthUpdate();
		}
	}

	public void removePlant(Plant plant) {
		ChunkCoord key = ChunkCoord.forLocation(plant.getLocation());
		ChunkCache cache = plantCache.getIfPresent(key);
		if (cache == null) {
			throw new IllegalStateException("Chunk for deleted plant was not loaded");
		}
		cache.removePlant(plant);
	}

	private void rescheduleGrowthUpdate() {
		if (schedulerTask != -1) {
			Bukkit.getScheduler().cancelTask(schedulerTask);
		}
		if (queueItems.isEmpty()) {
			schedulerTask = -1;
			return;
		}
		long now = System.currentTimeMillis();
		long nextGrowth = queueItems.iterator().next().getNextGrowthUpdateTime();
		if (nextGrowth == Long.MAX_VALUE) {
			schedulerTask = -1;
			return;
		}
		long toWait = nextGrowth - now;
		long ticksToWait = Math.max((toWait / 50) + 1, 0);
		System.out.println("Scheduling to run in " + ticksToWait);
		System.out.println(nextGrowth);
		schedulerTask = Bukkit.getScheduler().scheduleSyncDelayedTask(RealisticBiomes.getInstance(), () -> {
			while(true) {
				Iterator<ChunkCache> iter = queueItems.iterator();
				if (!iter.hasNext()) {
					break;
				}
				ChunkCache cache = iter.next();
				if (!cache.updatePlantState()) {
					break;
				}
			}
			schedulerTask = -1;
			rescheduleGrowthUpdate();
			
		}, ticksToWait);
	}

	void unloadChunkData(Chunk chunk) {
		ChunkCache cache;
		try {
			cache = plantCache.get(ChunkCoord.forChunk(chunk));
		} catch (ExecutionException e) {
			RealisticBiomes.getInstance().getLogger().severe("Failed to load chunk data: " + e.toString());
			return;
		}
		queueItems.remove(cache);
	}
}
