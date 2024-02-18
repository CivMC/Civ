package com.untamedears.realisticbiomes.growthconfig.inner;

import java.util.Map;
import org.bukkit.block.Biome;

/**
 * Whenever a plant grows, there's a certain chance for the growth to be
 * cancelled. A chance of 0.5 will effectively double the average growth time of
 * the specified plant
 *
 */
public class ChanceBasedGrowthConfig extends BiomeGrowthConfig {

	private double baseMultiplier;

	public ChanceBasedGrowthConfig(double baseMultiplier, Map<Biome, Double> biomeMapping) {
		super(biomeMapping);
		this.baseMultiplier = baseMultiplier;
	}

	@Override
	public double getNaturalProgressChance(Biome biome) {
		double biomeMult = getBiomeMultiplier(biome);
		return biomeMult * baseMultiplier;
	}

	@Override
	public boolean isPersistent() {
		return false;
	}

	@Override
	public String toString() {
		return "Chance based growth with " + baseMultiplier + " base multiplier";
	}

}
