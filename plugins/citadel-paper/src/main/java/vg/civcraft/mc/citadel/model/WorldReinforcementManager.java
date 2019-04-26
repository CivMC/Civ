package vg.civcraft.mc.citadel.model;

import java.util.Collection;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import vg.civcraft.mc.citadel.database.CitadelReinforcementData;

public class WorldReinforcementManager {

	private final int worldID;
	private final LoadingCache<ChunkCoord, ChunkCache> reinforcements;
	private World world;

	public WorldReinforcementManager(CitadelReinforcementData db, int worldID, World world) {
		this.worldID = worldID;
		this.world = world;
		this.reinforcements = CacheBuilder.newBuilder().removalListener(new RemovalListener<ChunkCoord, ChunkCache>() {
			@Override
			public void onRemoval(RemovalNotification<ChunkCoord, ChunkCache> removal) {
				ChunkCache chunk = removal.getValue();
				if (chunk.isDirty()) {
					db.saveReinforcements(chunk);
				}
			}
		}).build(new CacheLoader<ChunkCoord, ChunkCache>() {
			@Override
			public ChunkCache load(ChunkCoord loc) throws Exception {
				return db.loadReinforcements(loc, worldID, world);
			}
			/* probably not needed
			@Override
			public ListenableFuture<ChunkCache> reload(ChunkCoord loc, ChunkCache oldValue) throws Exception {
				// this will only be called in super rare race conditions
				return Futures.immediateFuture(oldValue);
			} */
		});
	}

	/**
	 * Gets all reinforcements in a chunk. Only use this if you know what you're
	 * doing
	 * 
	 * @param chunk Chunk to get reinforcements for
	 * @return All reinforcements within the chunk
	 */
	public Collection<Reinforcement> getAllReinforcements(Chunk chunk) {
		ChunkCache cache = reinforcements.getIfPresent(ChunkCoord.forChunk(chunk));
		if (cache == null) {
			throw new IllegalStateException("Can not retrieve reinforcements for unloaded chunks");
		}
		return cache.getAll();
	}

	/**
	 * Returns the Reinforcement for the specified block. World is not checked at
	 * this stage
	 * 
	 * @param block Location to get reinforcement for
	 * @return Reinforcement at the location if one exists, otherwise null
	 */
	public Reinforcement getReinforcement(Location loc) {
		if (loc == null) {
			throw new IllegalArgumentException("Can not get reinforcement for null location");
		}
		ChunkCache cache = reinforcements.getIfPresent(ChunkCoord.forLocation(loc));
		if (cache == null) {
			throw new IllegalStateException(
					"You can not check reinforcements for unloaded chunks. Load the chunk first");
		}
		Reinforcement rein = cache.getReinforcement(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		if (rein == null || rein.isBroken()) {
			return null;
		}
		return rein;
	}

	/**
	 * @return Internal id of the world for which this instance manages
	 *         reinforcements
	 */
	public int getWorldID() {
		return worldID;
	}

	public void insertReinforcement(Reinforcement reinforcement) {
		ChunkCoord key = ChunkCoord.forLocation(reinforcement.getLocation());
		ChunkCache cache = reinforcements.getIfPresent(key);
		if (cache == null) {
			throw new IllegalStateException("Chunk for created reinforcement was not loaded");
		}
		cache.insertReinforcement(reinforcement);
	}

	/**
	 * Used to flush all the reinforcements to the db on shutdown. Can be called
	 * else where if too a manual flush is wanted.
	 */
	public void invalidateAllReinforcements() {
		reinforcements.invalidateAll();
		reinforcements.cleanUp();
	}

	/**
	 * Checks if the location is reinforced or not.
	 * 
	 * @param loc The location of the potential reinforcement.
	 * @return True if a reinforcement exists at the given location, false otherwise
	 */
	public boolean isReinforced(Location loc) {
		return getReinforcement(loc) != null;
	}

	public void removeReinforcement(Reinforcement reinforcement) {
		ChunkCoord key = ChunkCoord.forLocation(reinforcement.getLocation());
		ChunkCache cache = reinforcements.getIfPresent(key);
		if (cache == null) {
			throw new IllegalStateException("Chunk for deleted reinforcement was not loaded");
		}
		cache.removeReinforcement(reinforcement);
	}
	
	/**
	 * @return World for which this instance is managing reinforcements
	 */
	public World getWorld() {
		return world;
	}

	void loadChunkData(Chunk chunk) {
		reinforcements.refresh(ChunkCoord.forChunk(chunk));
	}

	void unloadChunkData(Chunk chunk) {
		reinforcements.invalidate(ChunkCoord.forChunk(chunk));
	}
}
