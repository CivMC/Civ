package vg.civcraft.mc.civmodcore.locations.chunkmeta;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.citadel.model.WorldReinforcementManager;

public class GlobalChunkMetaManager {
	
	private Map<UUID, Integer> uuidToInternalID;
	private Map<UUID, WorldChunkMetaManager> worldToManager;
	private ChunkDAO chunkDao;
	
	public GlobalChunkMetaManager(ChunkDAO chunkDao) {
		this.uuidToInternalID = new TreeMap<>();
		this.worldToManager = new TreeMap<>();
		this.chunkDao = chunkDao;
	}
	
	
	public void registerRetrieval(JavaPlugin plugin, Class <? extends ChunkMeta> chunkMetaClass) {
		
	}
	
	/**
	 * Saves all data out to the database
	 */
	public void flushAll() {
		for (WorldChunkMetaManager man : worldToManager.values()) {
			man.flushAll();
		}
	}

	/**
	 * Gets all data in a chunk. Only use this if you know what you're
	 * doing
	 * 
	 * @param chunk Chunk to get data for
	 * @return All reinforcements within the chunk
	 */
	public Collection<Reinforcement> getAllReinforcements(Chunk chunk) {
		if (chunk == null) {
			throw new IllegalArgumentException("Chunk can not be null");
		}
		WorldReinforcementManager worldManager = worldToManager.get(chunk.getWorld().getUID());
		if (worldManager == null) {
			throw new IllegalStateException("No world manager for reinforcement in " + chunk.getWorld().toString());
		}
		return worldManager.getAllReinforcements(chunk);
	}

	/**
	 * Returns the Reinforcement for the specified block.
	 * 
	 * @param block Block to get reinforcement for
	 * @return Reinforcement of the block if one exists, otherwise null
	 */
	public Reinforcement getReinforcement(Block block) {
		return getReinforcement(block.getLocation());
	}

	/**
	 * Returns the Reinforcement for the specified block. World is not checked at
	 * this stage
	 * 
	 * @param block Location to get reinforcement for
	 * @return Reinforcement at the location if one exists, otherwise null
	 */
	public Reinforcement getReinforcement(Location location) {
		WorldReinforcementManager worldManager = worldToManager.get(location.getWorld().getUID());
		if (worldManager == null) {
			return null;
		}
		return worldManager.getReinforcement(location);
	}

	/**
	 * Inserts a new reinforcement into the cache. Should only be used for
	 * reinforcements created just now
	 * 
	 * @param loc  Location of the reinforcement
	 * @param rein Reinforcement created
	 */
	public void insertReinforcement(Reinforcement rein) {
		WorldReinforcementManager worldManager = worldToManager.get(rein.getLocation().getWorld().getUID());
		if (worldManager == null) {
			if (!registerWorld(rein.getLocation().getWorld())) {
				throw new IllegalStateException("Failed to register world");
			}
			worldManager = worldToManager.get(rein.getLocation().getWorld().getUID());
		}
		worldManager.insertReinforcement(rein);
	}

	public boolean registerWorld(World world) {
		int id = dao.getOrCreateWorldID(world);
		if (id == -1) {
			// very bad
			return false;
		}
		uuidToInternalID.put(world.getUID(), id);
		WorldReinforcementManager manager = new WorldReinforcementManager(dao, id, world);
		worldToManager.put(world.getUID(), manager);
		return true;
	}

	/**
	 * Removes a reinforcement from the immediate cache after it was destroyed
	 * 
	 * @param rein The reinforcement destroyed
	 */
	public void removeReinforcement(Reinforcement rein) {
		WorldReinforcementManager worldManager = worldToManager.get(rein.getLocation().getWorld().getUID());
		if (worldManager == null) {
			throw new IllegalStateException("No world manager for reinforcement at " + rein.getLocation().toString());
		}
		worldManager.removeReinforcement(rein);
	}

	public boolean setup() {
		for (World world : Bukkit.getWorlds()) {
			boolean worked = registerWorld(world);
			if (!worked) {
				return false;
			}
		}
		return true;
	}

	public void loadChunkData(Chunk chunk) {
		WorldChunkMetaManager worldManager = worldToManager.get(chunk.getWorld().getUID());
		if (worldManager == null) {
			throw new IllegalStateException("No world manager for chunk at " + chunk.toString());
		}
		worldManager.loadChunkData(chunk);
	}

	public void unloadChunkData(Chunk chunk) {
		WorldChunkMetaManager worldManager = worldToManager.get(chunk.getWorld().getUID());
		if (worldManager == null) {
			throw new IllegalStateException("No world manager for chunk at " + chunk.toString());
		}
		worldManager.unloadChunkData(chunk);
	}
	
	

}
