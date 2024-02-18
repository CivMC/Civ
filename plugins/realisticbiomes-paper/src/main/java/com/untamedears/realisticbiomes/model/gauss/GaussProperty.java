package com.untamedears.realisticbiomes.model.gauss;

import java.util.Random;

public final class GaussProperty {

	private static Random rng = new Random();

	private DeviatingDouble valueDeviation;
	private double sigma;
	private double value;
	
	private GaussProperty(DeviatingDouble valueDeviation, double sigma, double baseValue) {
		this.valueDeviation = valueDeviation;
		this.sigma = sigma;
		this.value = baseValue;
	}
	
	public static GaussProperty constructProperty(DeviatingDouble valueDeviation, double sigma) {
		return new GaussProperty(valueDeviation, sigma, 0);
	}
	
	public GaussProperty getDeviatedCopy() {
		return getDeviatedCopy(value);
	}
	
	public GaussProperty getDeviatedCopy(double baseValue) {
		return new GaussProperty(valueDeviation, sigma, valueDeviation.deviate(baseValue));
	}

	public double getRandomValue() {
		return rng.nextGaussian() * sigma * value + value;
	}

	public int getRoundedRandomValue() {
		return randomRound(getRandomValue());
	}

	private static int randomRound(double d) {
		double floor = Math.floor(d);
		double leftOver = d - Math.floor(d);
		if (rng.nextDouble() > leftOver) {
			return (int) floor;
		}
		return (int) floor + 1;
	}

}
