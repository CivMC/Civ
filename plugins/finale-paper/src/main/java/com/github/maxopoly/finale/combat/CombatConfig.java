package com.github.maxopoly.finale.combat;

public class CombatConfig {

	private int cpsLimit;
	private long cpsCounterInterval;
	private boolean noCooldown;
	private double maxReach;
	private boolean sweepEnabled;
	private CombatSoundConfig combatSounds;
	private double horizontalKb;
	private double verticalKb;

	public CombatConfig(boolean noCooldown, int cpsLimit, long cpsCounterInterval, double maxReach, boolean sweepEnabled, CombatSoundConfig combatSounds,
			double horizontalKb, double verticalKb) {
		this.noCooldown = noCooldown;
		this.cpsLimit = cpsLimit;
		this.cpsCounterInterval = cpsCounterInterval;
		this.maxReach = maxReach;
		this.sweepEnabled = sweepEnabled;
		this.combatSounds = combatSounds;
		this.horizontalKb = horizontalKb;
		this.verticalKb = verticalKb;
	}
	
	public int getCPSLimit() {
		return cpsLimit;
	}
	
	public long getCpsCounterInterval() {
		return cpsCounterInterval;
	}
	
	public boolean isNoCooldown() {
		return noCooldown;
	}
	
	public double getMaxReach() {
		return maxReach;
	}
	
	public boolean isSweepEnabled() {
		return sweepEnabled;
	}
	
	public CombatSoundConfig getCombatSounds() {
		return combatSounds;
	}
	
	public double getHorizontalKB() {
		return horizontalKb;
	}
	
	public double getVerticalKB() {
		return verticalKb;
	}
}
