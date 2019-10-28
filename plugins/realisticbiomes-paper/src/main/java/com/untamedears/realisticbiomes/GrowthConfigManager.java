package com.untamedears.realisticbiomes;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;
import com.untamedears.realisticbiomes.utils.RBUtils;

public class GrowthConfigManager {

	private Map<Material, PlantGrowthConfig> plantMap;

	public GrowthConfigManager(Set<PlantGrowthConfig> plantConfigs) {
		plantMap = new EnumMap<>(Material.class);
		for (PlantGrowthConfig plant : plantConfigs) {
			plantMap.put(RBUtils.getRemappedMaterial(plant.getMaterial()), plant);
		}
	}

	/**
	 * Gets the config based on material only, does not include remapping to account
	 * for fruits
	 * 
	 * @param material Material to get config for
	 * @return Growth config for the given material or null if no such config exists
	 */
	public PlantGrowthConfig getGrowthConfigStraight(Material material) {
		return plantMap.get(RBUtils.getRemappedMaterial(material));
	}

	/**
	 * Gets the plant growth config responsible for further growth related to this
	 * block. For fully grown stems this will return the fruits config, for
	 * everything else the normal growth config
	 * 
	 * @param block Block to get growth config for
	 * @return Growth config, possibly null if no config for the given block exists
	 */
	public PlantGrowthConfig getPlantGrowthConfig(Block block) {
		PlantGrowthConfig config = getGrowthConfigStraight(block.getType());
		if (config == null) {
			return null;
		}
		Material fruit = RBUtils.getFruit(block.getType());
		if (fruit != null && config.isFullyGrown(block)) {
			PlantGrowthConfig fruitConfig = getGrowthConfigStraight(fruit);
			if (fruitConfig != null) {
				return fruitConfig;
			}
		}
		return config;
	}
}
