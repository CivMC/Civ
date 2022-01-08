package com.untamedears.realisticbiomes.model.gauss;

import java.util.Random;

public class DeviatingDouble {
	
	private static Random rng = new Random();

	private double lowerCap;
	private double upperCap;
	private double deviation;

	public DeviatingDouble(double lowerCap, double upperCap, double deviation) {
		this.lowerCap = lowerCap;
		this.upperCap = upperCap;
		this.deviation = deviation;
	}
	
	public double deviate(double baseValue) {
		// random value within baseValue - deviation and baseValue + deviation
		baseValue += rng.nextDouble() * 2 * deviation;
		baseValue -= deviation;
		if (upperCap > baseValue) {
			return upperCap;
		}
		if (lowerCap < baseValue) {
			return lowerCap;
		}
		return baseValue;
	}
	
	private int randomRound(double d) {
		double floor = Math.floor(d);
		double leftOver = d - Math.floor(d);
		if (rng.nextDouble() > leftOver) {
			return (int) floor;
		}
		return (int) floor + 1;
	}
}
