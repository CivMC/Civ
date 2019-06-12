package com.untamedears.realisticbiomes;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;

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

	public PlantGrowthConfig getPlantGrowthConfig(Material material) {
		return plantMap.get(RBUtils.getRemappedMaterial(material));
	}
}
