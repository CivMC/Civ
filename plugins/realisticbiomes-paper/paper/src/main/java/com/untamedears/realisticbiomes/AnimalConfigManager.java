package com.untamedears.realisticbiomes;

import com.untamedears.realisticbiomes.growthconfig.AnimalMateConfig;
import java.util.HashMap;
import org.bukkit.entity.EntityType;

public class AnimalConfigManager {
	private HashMap<EntityType, AnimalMateConfig> entityMap;

	public AnimalConfigManager() {
		entityMap = new HashMap<>();
	}

	public AnimalMateConfig getAnimalMateConfig(EntityType entity) {
		return entityMap.get(entity);
	}

}
