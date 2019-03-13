package vg.civcraft.mc.citadel;

import org.bukkit.Location;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import vg.civcraft.mc.citadel.database.CitadelReinforcementData;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;

public class ReinforcementManager {

	private final int worldID;
	private final LoadingCache<ChunkPair, ChunkCache> reinforcements;

	public ReinforcementManager(CitadelReinforcementData db, int worldID) {
		this.worldID = worldID;
		this.reinforcements = CacheBuilder.newBuilder().removalListener(new RemovalListener<ChunkPair, ChunkCache>() {
			public void onRemoval(RemovalNotification<ChunkPair, ChunkCache> removal) {
				ChunkCache chunk = removal.getValue();
				if (chunk.isDirty()) {
					db.saveReinforcements(chunk);
				}
			}
		}).build(new CacheLoader<ChunkPair, ChunkCache>() {
			public ChunkCache load(ChunkPair loc) throws Exception {
				if (loc == null) {
					throw new IllegalArgumentException("Can not load reinforcements for null location");
				}
				return db.loadReinforcements(loc, worldID);
			}
		});
	}
	
	public void insertReinforcement(Reinforcement reinforcement) {
		ChunkPair key = ChunkPair.forLocation(reinforcement.getLocation());
		ChunkCache cache = reinforcements.getIfPresent(key);
		if (cache == null) {
			throw new IllegalStateException("Chunk for created reinforcement was not loaded");
		}
		cache.insertReinforcement(reinforcement);
	}
	
	public void removeReinforcement(Reinforcement reinforcement) {
		ChunkPair key = ChunkPair.forLocation(reinforcement.getLocation());
		ChunkCache cache = reinforcements.getIfPresent(key);
		if (cache == null) {
			throw new IllegalStateException("Chunk for deleted reinforcement was not loaded");
		}
		cache.removeReinforcement(reinforcement);
	}
	
	/**
	 * @return Internal id of the world for which this instance manages reinforcements
	 */
	public int getWorldID() {
		return worldID;
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
		ChunkCache cache = reinforcements.getIfPresent(ChunkPair.forLocation(loc));
		if (cache == null) {
			throw new IllegalStateException(
					"You can not check reinforcements for unloaded chunks. Load the chunk first");
		}
		Reinforcement rein = cache.getReinforcement(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		if (rein.isBroken()) {
			return null;
		}
		return rein;
	}

	/**
	 * Used to flush all the reinforcements to the db on shutdown. Can be called
	 * else where if too a manual flush is wanted.
	 */
	public void invalidateAllReinforcements() {
		reinforcements.invalidateAll();
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
}
