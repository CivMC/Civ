package com.untamedears.realisticbiomes.growthconfig.inner;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.bukkit.block.Biome;
import vg.civcraft.mc.civmodcore.utilities.TextUtil;

/**
 * Bases growth times on real life time. Biome based multipliers apply inverse
 * here, so a growth multiplier of 2.0 will half the duration needed
 *
 */
public class PersistentGrowthConfig extends BiomeGrowthConfig {

	/**
	 * Base growth time in ms
	 */
	private long timeNeeded;

	public PersistentGrowthConfig(long growthTime, Map<Biome, Double> biomeMapping) {
		super(biomeMapping);
		this.timeNeeded = growthTime;
	}

	@Override
	public double getNaturalProgressChance(Biome biome) {
		return 0.0;
	}

	public long getTotalGrowthTimeNeeded(Biome biome) {
		double biomeMult = getBiomeMultiplier(biome);
		return (long) (timeNeeded / biomeMult);
	}

	@Override
	public boolean isPersistent() {
		return true;
	}

	@Override
	public String toString() {
		return "Persistent growth with " + TextUtil.formatDuration(timeNeeded, TimeUnit.MILLISECONDS) + " duration";
	}
}
