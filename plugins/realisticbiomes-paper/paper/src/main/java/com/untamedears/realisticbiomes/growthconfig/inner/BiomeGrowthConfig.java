package com.untamedears.realisticbiomes.growthconfig.inner;

import java.util.Map;
import org.bukkit.block.Biome;

public abstract class BiomeGrowthConfig {

	protected final Map<Biome, Double> biomeMultipliers;

	public BiomeGrowthConfig(Map<Biome, Double> biomeMapping) {
		this.biomeMultipliers = biomeMapping;
	}

	public boolean canGrowIn(Biome biome) {
		return getBiomeMultiplier(biome) > 0;
	}

	/**
	 * Gets the multiplier applied by this config in a specific biome
	 * 
	 * @param biome Biome to get multiplier for
	 * @return Growth multiplier for the biome, will be 0 if none is specfified
	 */
	public double getBiomeMultiplier(Biome biome) {
		Double mult = biomeMultipliers.get(biome);
		if (mult == null) {
			return 0;
		}
		return mult;
	}

	public abstract double getNaturalProgressChance(Biome biome);

	public abstract boolean isPersistent();

}
