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
	private double sprintHorizontal;
	private double sprintVertical;
	private double airHorizontal;
	private double airVertical;
	private double waterHorizontal;
	private double waterVertical;
	private double attackMotionModifier;
	private boolean stopSprinting;
	private double potionCutOffDistance;

	public CombatConfig(boolean noCooldown, int cpsLimit, long cpsCounterInterval, double maxReach, boolean sweepEnabled, CombatSoundConfig combatSounds,
			double horizontalKb, double verticalKb, double sprintHorizontal, double sprintVertical, double airHorizontal, double airVertical,
			double waterHorizontal, double waterVertical, double attackMotionModifier, boolean stopSprinting, double potionCutOffDistance) {
		this.noCooldown = noCooldown;
		this.cpsLimit = cpsLimit;
		this.cpsCounterInterval = cpsCounterInterval;
		this.maxReach = maxReach;
		this.sweepEnabled = sweepEnabled;
		this.combatSounds = combatSounds;
		this.horizontalKb = horizontalKb;
		this.verticalKb = verticalKb;
		this.sprintHorizontal = sprintHorizontal;
		this.sprintVertical = sprintVertical;
		this.airHorizontal = airHorizontal;
		this.airVertical = airVertical;
		this.waterHorizontal = waterHorizontal;
		this.waterVertical = waterVertical;
		this.attackMotionModifier = attackMotionModifier;
		this.stopSprinting = stopSprinting;
		this.potionCutOffDistance = potionCutOffDistance;
	}
	
	public void setPotionCutOffDistance(double potionCutOffDistance) {
		this.potionCutOffDistance = potionCutOffDistance;
	}
	
	public double getPotionCutOffDistance() {
		return potionCutOffDistance;
	}
	
	public void setHorizontalKb(double horizontalKb) {
		this.horizontalKb = horizontalKb;
	}
	
	public void setVerticalKb(double verticalKb) {
		this.verticalKb = verticalKb;
	}
	
	public void setSprintHorizontal(double sprintHorizontal) {
		this.sprintHorizontal = sprintHorizontal;
	}
	
	public void setSprintVertical(double sprintVertical) {
		this.sprintVertical = sprintVertical;
	}

	public void setAirHorizontal(double airHorizontal) {
		this.airHorizontal = airHorizontal;
	}
	
	public void setAirVertical(double airVertical) {
		this.airVertical = airVertical;
	}
	
	public void setWaterHorizontal(double waterHorizontal) {
		this.waterHorizontal = waterHorizontal;
	}
	
	public void setWaterVertical(double waterVertical) {
		this.waterVertical = waterVertical;
	}
	
	public void setAttackMotionModifier(double attackMotionModifier) {
		this.attackMotionModifier = attackMotionModifier;
	}
	
	public void setStopSprinting(boolean stopSprinting) {
		this.stopSprinting = stopSprinting;
	}
	
	public double getAirHorizontal() {
		return airHorizontal;
	}
	
	public double getAirVertical() {
		return airVertical;
	}
	
	public double getWaterHorizontal() {
		return waterHorizontal;
	}
	
	public double getWaterVertical() {
		return waterVertical;
	}
	
	public boolean isStopSprinting() {
		return stopSprinting;
	}
	
	public double getHorizontalKb() {
		return horizontalKb;
	}
	
	public double getVerticalKb() {
		return verticalKb;
	}
	
	public double getSprintHorizontal() {
		return sprintHorizontal;
	}
	
	public double getSprintVertical() {
		return sprintVertical;
	}
	
	public double getAttackMotionModifier() {
		return attackMotionModifier;
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
