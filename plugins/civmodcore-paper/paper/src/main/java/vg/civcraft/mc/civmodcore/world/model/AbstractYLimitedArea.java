package vg.civcraft.mc.civmodcore.world.model;

import org.bukkit.Location;

public abstract class AbstractYLimitedArea implements IArea {

	private double lowerYBound;

	private double upperYBound;

	public AbstractYLimitedArea(double lowerYBound, double upperYBound) {
		if (lowerYBound > upperYBound) {
			throw new IllegalArgumentException("Lower bound can't be bigger upper one");
		}
		this.lowerYBound = lowerYBound;
		this.upperYBound = upperYBound;
	}

	@Override
	public boolean isInArea(Location loc) {
		return loc.getY() <= upperYBound && loc.getY() >= lowerYBound;
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
