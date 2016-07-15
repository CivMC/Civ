package vg.civcraft.mc.civmodcore.areas;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.block.Biome;

public abstract class AbstractYLimitedArea implements IArea {
	
	
	private double lowerYBound;
	private double upperYBound;
	private Collection <Biome> allowedBiomes;
	
	
	public AbstractYLimitedArea(double lowerYBound, double upperYBound, Collection <Biome> allowedBiomes) {
		if (lowerYBound > upperYBound) {
			throw new IllegalArgumentException("Lower bound can't be bigger upper one");
		}
		this.lowerYBound = lowerYBound;
		this.upperYBound = upperYBound;
		this.allowedBiomes = allowedBiomes;
	}

	@Override
	public boolean isInArea(Location loc) {
		return loc.getY() <= upperYBound && loc.getY() >= lowerYBound && (allowedBiomes == null || allowedBiomes.contains(loc.getBlock().getBiome()));
	}
	
	/**
	 * @return The lowest y-level from which upwards locations can be included in this area (inclusive)
	 */
	public double getLowerYBound() {
		return lowerYBound;
	}
	
	/**
	 * @return The highest y-level from which downwards location can be included in this area (inclusive)
	 */
	public double getUpperYBound() {
		return upperYBound;
	}
}
