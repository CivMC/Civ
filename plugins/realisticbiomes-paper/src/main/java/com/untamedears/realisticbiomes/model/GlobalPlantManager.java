package com.untamedears.realisticbiomes.model;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class GlobalPlantManager {

	private Map<UUID, Integer> uuidToInternalID;
	private Map<UUID, WorldPlantManager> worldToManager;
	private RBDAO dao;

	public GlobalPlantManager(RBDAO dao) {
		this.uuidToInternalID = new TreeMap<>();
		this.worldToManager = new TreeMap<>();
		this.dao = dao;
	}

	/**
	 * Saves all plants out to the database
	 */
	public void flushAll() {
		for (WorldPlantManager man : worldToManager.values()) {
			man.invalidateAllPlants();
		}
	}

	/**
	 * Returns the Plant tracked for the specified block.
	 * 
	 * @param block Block to get plant for
	 * @return Plant tracked for the block if one exists, otherwise null
	 */
	public Plant getPlant(Block block) {
		return getPlant(block.getLocation());
	}

	/**
	 * Returns the Plant for the specified block. World is not checked at
	 * this stage
	 * 
	 * @param block Location to get Plant for
	 * @return Plant tracked at the location if one exists, otherwise null
	 */
	public Plant getPlant(Location location) {
		WorldPlantManager worldManager = worldToManager.get(location.getWorld().getUID());
		if (worldManager == null) {
			return null;
		}
		return worldManager.getPlant(location);
	}

	/**
	 * Checks if the given block has a tracked plant or not.
	 * 
	 * @param b The block of the potential plant.
	 * @return True if a tracked plant exists for the given block, false otherwise
	 */
	public boolean hasTrackedPlant(Block b) {
		return hasTrackedPlant(b.getLocation());
	}

	/**
	 * Checks if the block a the given location is a tracked plant or not. Priorize
	 * using getPlant() and doing a null check yourself if you intend to do
	 * something with the plant to avoid a double lookup
	 * 
	 * @param loc The location of the potential plant.
	 * @return True if a tracked plant exists at the given location, false otherwise
	 */
	public boolean hasTrackedPlant(Location loc) {
		return getPlant(loc) != null;
	}

	/**
	 * Inserts a new plant into the cache. Should only be used for
	 * plants created just now
	 * 
	 * @param plant Plant created
	 */
	public void insertPlant(Plant plant) {
		WorldPlantManager worldManager = worldToManager.get(plant.getLocation().getWorld().getUID());
		if (worldManager == null) {
			if (!registerWorld(plant.getLocation().getWorld())) {
				throw new IllegalStateException("Failed to register world");
			}
			worldManager = worldToManager.get(plant.getLocation().getWorld().getUID());
		}
		worldManager.insertPlant(plant);
	}

	public void loadChunkData(Chunk chunk) {
		WorldPlantManager worldManager = worldToManager.get(chunk.getWorld().getUID());
		worldManager.loadChunkData(chunk);
	}

	public boolean registerWorld(World world) {
		int id = dao.getOrCreateWorldID(world);
		if (id == -1) {
			// very bad
			return false;
		}
		uuidToInternalID.put(world.getUID(), id);
		WorldPlantManager manager = new WorldPlantManager(dao, id, world);
		worldToManager.put(world.getUID(), manager);
		return true;
	}

	/**
	 * Removes a plant from the immediate cache after it was destroyed
	 * 
	 * @param plant The plant destroyed
	 */
	public void removePlant(Plant plant) {
		WorldPlantManager worldManager = worldToManager.get(plant.getLocation().getWorld().getUID());
		worldManager.removePlant(plant);
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

	public void unloadChunkData(Chunk chunk) {
		WorldPlantManager worldManager = worldToManager.get(chunk.getWorld().getUID());
		worldManager.unloadChunkData(chunk);
	}
}
