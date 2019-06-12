package com.untamedears.realisticbiomes.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.untamedears.realisticbiomes.GrowthConfigManager;
import com.untamedears.realisticbiomes.RealisticBiomes;
import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;

public class ChunkCache {

	private final ChunkCoord chunkPair;
	private Map<Coords, Plant> plants;
	private List<Plant> deletedPlants;
	private boolean isDirty;
	private final WorldPlantManager worldManager;
	private TreeSet<Plant> queueItems;
	private long nextGrowthUpdate;

	public ChunkCache(WorldPlantManager worldManager, ChunkCoord chunkPair, Collection<Plant> plants) {
		this.plants = new TreeMap<>();
		this.worldManager = worldManager;
		this.nextGrowthUpdate = Long.MAX_VALUE;
		this.queueItems = new TreeSet<>((a, b) -> Long.compare(a.getNextGrowthTime(), b.getNextGrowthTime()));
		GrowthConfigManager growthConfigManager = RealisticBiomes.getInstance().getGrowthConfigManager();
		for (Plant plant : plants) {
			plant.setOwningCache(this);
			Block block = plant.getLocation().getBlock();
			PlantGrowthConfig growthConfig = growthConfigManager.getPlantGrowthConfig(block.getType());
			if (growthConfig == null || !growthConfig.isPersistent()) {
				deletedPlants.add(plant);
				plant.delete();
				continue;
			}
			plant.innerUpdateGrowthTime(growthConfig.updatePlant(plant, block));
			this.plants.put(new Coords(plant.getLocation()), plant);
			queueItems.add(plant);
			nextGrowthUpdate = Long.min(nextGrowthUpdate, plant.getNextGrowthTime());
		}
		this.chunkPair = chunkPair;
		this.isDirty = false;
	}

	/**
	 * Gets all existing plants within this chunk
	 * 
	 * @return All plants
	 */
	public Collection<Plant> getAll() {
		List<Plant> plantList = new ArrayList<>();
		plantList.addAll(plants.values());
		return plantList;
	}

	/**
	 * Used when dumping all plants to the database. Returns not only the existing
	 * plants, but also the ones deleted and not yet removed from the database
	 * 
	 * @return All plants possibly needing to be persisted to the database
	 */
	public Collection<Plant> getAllAndCleanUp() {
		List<Plant> plantList = new ArrayList<>();
		if (deletedPlants != null) {
			plantList.addAll(deletedPlants);
			deletedPlants.clear();
		}
		plantList.addAll(plants.values());
		return plantList;
	}

	public ChunkCoord getChunkPair() {
		return chunkPair;
	}

	public long getNextGrowthUpdateTime() {
		return nextGrowthUpdate;
	}

	public Plant getPlant(int x, int y, int z) {
		return plants.get(new Coords(x, y, z));
	}

	public int getWorldID() {
		return worldManager.getWorldID();
	}

	public void insertPlant(Plant plant) {
		Coords key = new Coords(plant.getLocation());
		if (plants.containsKey(key)) {
			throw new IllegalStateException(
					"Trying to insert plant at " + plant.getLocation().toString() + ", but one already existed");
		}
		plant.setOwningCache(this);
		plants.put(key, plant);
		if (plant.isDirty()) {
			this.isDirty = true;
		}
		updateNextGrowthTime(plant, plant.getNextGrowthTime());
	}

	public boolean isDirty() {
		return isDirty;
	}

	public void removePlant(Plant plant) {
		Coords key = new Coords(plant.getLocation());
		Plant removed = plants.remove(key);
		if (removed != plant) {
			throw new IllegalStateException("Removed wrong plant at " + plant.getLocation().toString());
		}
		if (deletedPlants == null) {
			deletedPlants = new LinkedList<>();
		}
		deletedPlants.add(plant);
		queueItems.remove(plant);
	}

	public void setDirty(boolean dirty) {
		this.isDirty = dirty;
	}

	void updateNextGrowthTime(Plant plant, long newTime) {
		System.out.println("Updating time in cache to " + newTime);
		long oldTime = plant.getNextGrowthTime();
		if (oldTime != newTime) {
			queueItems.remove(plant);
			plant.innerUpdateGrowthTime(newTime);
		}
		queueItems.add(plant);
		if (newTime < nextGrowthUpdate || nextGrowthUpdate < System.currentTimeMillis()) {
			worldManager.removeFromGrowthQueue(this);
			nextGrowthUpdate = newTime;
			worldManager.addToGrowthQueue(this);
		}
	}

	boolean updatePlantState() {
		Iterator<Plant> iter = queueItems.iterator();
		List<Plant> toDelete = new LinkedList<>();
		Map<Plant, Long> newTimings = new HashMap<>();
		boolean didSomething = false;
		GrowthConfigManager configManager = RealisticBiomes.getInstance().getGrowthConfigManager();
		while (iter.hasNext()) {
			Plant plant = iter.next();
			Block block = plant.getLocation().getBlock();
			Material material = block.getType();
			PlantGrowthConfig config = configManager.getPlantGrowthConfig(material);
			if (config == null) {
				toDelete.add(plant);
				continue;
			}
			long nextUpdate = config.updatePlant(plant, block);
			if (nextUpdate != plant.getNextGrowthTime()) {
				didSomething = true;
				newTimings.put(plant, nextUpdate);
			} else {
				break;
			}
		}
		for (Plant plant : toDelete) {
			worldManager.removePlant(plant);
		}
		for (Entry<Plant, Long> entry : newTimings.entrySet()) {
			updateNextGrowthTime(entry.getKey(), entry.getValue());
		}
		if (queueItems.isEmpty()) {
			worldManager.removeFromGrowthQueue(this);
		}
		return didSomething;
	}
}
