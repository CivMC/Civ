package com.github.maxopoly.finale.combat;

public class CombatConfig {

	private int cpsLimit;
	private double maxReach;
	private double critMultiplier;
	
	public CombatConfig(int cpsLimit, double maxReach, double critMultiplier) {
		this.cpsLimit = cpsLimit;
		this.maxReach = maxReach;
		this.critMultiplier = critMultiplier;
	}
	
	public int getCPSLimit() {
		return cpsLimit;
	}
	
	public double getMaxReach() {
		return maxReach;
	}
	
	public double getCritMultiplier() {
		return critMultiplier;
	}
}
