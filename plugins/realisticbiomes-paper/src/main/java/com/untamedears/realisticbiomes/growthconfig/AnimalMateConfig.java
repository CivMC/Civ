package com.untamedears.realisticbiomes.growthconfig;

import com.untamedears.realisticbiomes.growthconfig.inner.BiomeGrowthConfig;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;

public class AnimalMateConfig extends AbstractGrowthConfig {

	private EntityType type;
	private BiomeGrowthConfig biomeConfig;

	public AnimalMateConfig(String name, EntityType type, BiomeGrowthConfig biomeConfig) {
		super(name);
		this.type = type;
		this.biomeConfig = biomeConfig;
	}

	public EntityType getEntityType() {
		return type;
	}

	public double getRate(Biome biome) {
		return biomeConfig.getBiomeMultiplier(biome);
	}
}
