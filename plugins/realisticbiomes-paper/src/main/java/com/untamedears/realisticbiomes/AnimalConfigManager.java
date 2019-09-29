package com.untamedears.realisticbiomes;

import java.util.HashMap;

import org.bukkit.entity.EntityType;

import com.untamedears.realisticbiomes.growthconfig.AnimalMateConfig;

public class AnimalConfigManager {
	private HashMap<EntityType, AnimalMateConfig> entityMap;

	public AnimalConfigManager() {
		entityMap = new HashMap<>();
	}

	public AnimalMateConfig getAnimalMateConfig(EntityType entity) {
		return entityMap.get(entity);
	}

}
