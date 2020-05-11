package com.untamedears.realisticbiomes;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;
import com.untamedears.realisticbiomes.utils.RBUtils;

public class GrowthConfigManager {

	private Map<Material, PlantGrowthConfig> fallbackPlantMap;
	private Map<Short, PlantGrowthConfig> plantsById;

	public GrowthConfigManager(Set<PlantGrowthConfig> plantConfigs) {
		fallbackPlantMap = new EnumMap<>(Material.class);
		plantsById = new HashMap<>();
		for (PlantGrowthConfig plant : plantConfigs) {
			for(Material mat : plant.getApplicableVanillaPlants()) {
				fallbackPlantMap.put(mat, plant);
			}
			plantsById.put(plant.getID(), plant);
		}
	}
	
	public PlantGrowthConfig getConfigById(short id) {
		return plantsById.get(id);
	}

	/**
	 * Gets the config based on material only, does not include remapping to account
	 * for fruits, any special block tracking etc.
	 * 
	 * @param material Material to get config for
	 * @return Growth config for the given material or null if no such config exists
	 */
	public PlantGrowthConfig getGrowthConfigFallback(Material material) {
		return fallbackPlantMap.get(material);
	}

	/**
	 * Gets the plant growth config responsible for further growth related to this
	 * block. For fully grown stems this will return the fruits config, for
	 * everything else the normal growth config
	 * 
	 * @param block Block to get growth config for
	 * @return Growth config, possibly null if no config for the given block exists
	 */
	public PlantGrowthConfig getPlantGrowthConfigFallback(Block block) {
		PlantGrowthConfig config = getGrowthConfigFallback(block.getType());
		if (config == null) {
			return null;
		}
		Material fruit = RBUtils.getFruit(block.getType());
		if (fruit != null && config.isFullyGrown(block)) {
			PlantGrowthConfig fruitConfig = getGrowthConfigFallback(fruit);
			if (fruitConfig != null) {
				return fruitConfig;
			}
		}
		return config;
	}
}
