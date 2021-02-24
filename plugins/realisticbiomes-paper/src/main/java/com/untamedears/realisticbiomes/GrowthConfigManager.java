package com.untamedears.realisticbiomes;

import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;
import com.untamedears.realisticbiomes.model.Plant;
import com.untamedears.realisticbiomes.utils.RBUtils;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class GrowthConfigManager {

	private Map<Material, PlantGrowthConfig> fallbackPlantMap;
	private Map<Short, PlantGrowthConfig> plantsById;
	private Map<ItemStack, PlantGrowthConfig> plantsByItem;

	public GrowthConfigManager(Set<PlantGrowthConfig> plantConfigs) {
		fallbackPlantMap = new EnumMap<>(Material.class);
		plantsById = new HashMap<>();
		plantsByItem = new HashMap<>();
		for (PlantGrowthConfig plant : plantConfigs) {
			for(Material mat : plant.getApplicableVanillaPlants()) {
				fallbackPlantMap.put(mat, plant);
			}
			if (plant.getItem() != null) {
				plantsByItem.put(plant.getItem(), plant);
			}
			plantsById.put(plant.getID(), plant);
		}
	}
	
	public Set<PlantGrowthConfig> getAllGrowthConfigs() {
		return new HashSet<>(plantsById.values());
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
	 * Retrieves a growth config by the Item used to plant it
	 * @param item ItemStack used to plant the item
	 * @return Growth config for the given item if one exists
	 */
	public PlantGrowthConfig getGrowthConfigByItem(ItemStack item) {
		ItemStack copy = item.clone();
		copy.setAmount(1);
		return plantsByItem.get(copy);
	}

	/**
	 * Gets the plant growth config responsible for further growth related to this
	 * plant based on its block state. For fully grown stems this will return the fruits config, for
	 * everything else the normal growth config
	 * 
	 * @param plant Plant to get growth config for
	 * @return Growth config, possibly null if no config for the block of the given plant exists
	 */
	public PlantGrowthConfig getPlantGrowthConfigFallback(Plant plant) {
		Block block = plant.getLocation().getBlock();
		PlantGrowthConfig config = getGrowthConfigFallback(block.getType());
		if (config == null) {
			return null;
		}
		Material fruit = RBUtils.getFruit(block.getType());
		if (fruit != null && config.isFullyGrown(new Plant(block.getLocation(), config))) {
			PlantGrowthConfig fruitConfig = getGrowthConfigFallback(fruit);
			if (fruitConfig != null) {
				plant.setGrowthConfig(fruitConfig);
				return fruitConfig;
			}
		}
		plant.setGrowthConfig(config);
		return config;
	}
}
