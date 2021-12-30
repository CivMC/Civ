package com.github.devotedmc.hiddenore;

import org.bukkit.Location;
import org.bukkit.util.noise.NoiseGenerator;
import org.bukkit.util.noise.SimplexNoiseGenerator;

/**
 * Borrowed wholesale, for now, from CivClassic vein's approach branch.
 * Might change over time.
 * 
 * @author TealNerd, ProgrammerDan
 *
 */
public class VeinConfig {
	
	private double density;
	private double maxSpan;
	private double densityBonus;
	private double areaHeight;
	private double areaSpan;
	private double heightLength;
	private double densityLength;
	
	private boolean forceVisibleTransform;

	private NoiseGenerator heightNoiseGen;
	private NoiseGenerator densityNoiseGen;
	
	
	public VeinConfig(long densitySeed, long heightSeed, double density, double maxSpan, double densityBonus, double areaHeight,
			double areaSpan, double heightLength, double densityLength, boolean forceVisibleTransform) {
		this.density = density;
		this.maxSpan = maxSpan;
		this.densityBonus = densityBonus;
		this.areaHeight = areaHeight;
		this.areaSpan = areaSpan;
		this.heightLength = heightLength;
		this.densityLength = densityLength;
		this.heightNoiseGen = new SimplexNoiseGenerator(heightSeed);
		this.densityNoiseGen = new SimplexNoiseGenerator(densitySeed);
		this.forceVisibleTransform = forceVisibleTransform;
	}

	public double getOreChance(Location loc) {
		return getOreChance(loc.getX(), loc.getBlockY(), loc.getZ());
	}
	
	public double getOreChance(double x, int y, double z) {
		double chance = Math.abs(y-getVeinHeight(x, z));
		if(chance > maxSpan) return 0;
		return ((Math.cos(chance * Math.PI / maxSpan) + 1) / 2) * getVeinDensity(x, z);
	}
	
	private double getVeinHeight(double x, double z) {
		return heightNoiseGen.noise(x / heightLength, z / heightLength) * areaSpan + areaHeight;
	}
	
	private double getVeinDensity(double x, double z) {
		return (densityNoiseGen.noise(x / densityLength, z / densityLength) + densityBonus) * density;
	}
	
	public boolean getForceVisibleTransform() {
		return this.forceVisibleTransform;
	}
}