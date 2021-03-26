package com.github.maxopoly.finale.combat;

import com.github.maxopoly.finale.Finale;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;

public class CombatConfig {

	private int cpsLimit;
	private long cpsCounterInterval;
	private boolean attackCooldownEnabled;
	private boolean knockbackSwordsEnabled;
	private boolean sprintResetEnabled;
	private boolean waterSprintResetEnabled;
	private double maxReach;
	private boolean sweepEnabled;
	private CombatSoundConfig combatSounds;
	private double knockbackLevelMultiplier;
	private Vector knockbackMultiplier;
	private Vector sprintMultiplier;
	private Vector waterKnockbackMultiplier;
	private Vector airKnockbackMultiplier;
	private Vector victimMotion;
	private Vector maxVictimMotion;
	private Vector attackerMotion;

	public CombatConfig(boolean attackCooldownEnabled, boolean knockbackSwordsEnabled, boolean sprintResetEnabled, boolean waterSprintResetEnabled, int cpsLimit, long cpsCounterInterval, double maxReach, boolean sweepEnabled, CombatSoundConfig combatSounds,
						double knockbackLevelMultiplier, Vector knockbackMultiplier, Vector sprintMultiplier, Vector waterKnockbackMultiplier, Vector airKnockbackMultiplier, Vector victimMotion, Vector maxVictimMotion,
						Vector attackerMotion) {
		this.attackCooldownEnabled = attackCooldownEnabled;
		this.knockbackSwordsEnabled = knockbackSwordsEnabled;
		this.sprintResetEnabled = sprintResetEnabled;
		this.waterSprintResetEnabled = waterSprintResetEnabled;
		this.cpsLimit = cpsLimit;
		this.cpsCounterInterval = cpsCounterInterval;
		this.maxReach = maxReach;
		this.sweepEnabled = sweepEnabled;
		this.combatSounds = combatSounds;
		this.knockbackLevelMultiplier = knockbackLevelMultiplier;
		this.knockbackMultiplier = knockbackMultiplier;
		this.sprintMultiplier = sprintMultiplier;
		this.waterKnockbackMultiplier = waterKnockbackMultiplier;
		this.airKnockbackMultiplier = airKnockbackMultiplier;
		this.victimMotion = victimMotion;
		this.maxVictimMotion = maxVictimMotion;
		this.attackerMotion = attackerMotion;
	}

	private void setVector(FileConfiguration config, String name, Vector vec) {
		config.set(name + ".x", vec.getX());
		config.set(name + ".y", vec.getY());
		config.set(name + ".z", vec.getZ());
	}

	public void save() {
		FileConfiguration config = Finale.getPlugin().getConfig();
		setVector(config, "clearCombat.knockbackMultiplier", this.knockbackMultiplier);
		setVector(config, "clearCombat.sprintMultiplier", this.sprintMultiplier);
		setVector(config, "clearCombat.waterKnockbackMultiplier", this.waterKnockbackMultiplier);
		setVector(config, "clearCombat.airKnockbackMultiplier", this.airKnockbackMultiplier);
		setVector(config, "clearCombat.victimMotion", this.victimMotion);
		setVector(config, "clearCombat.maxVictimMotion", this.maxVictimMotion);
		setVector(config, "clearCombat.attackerMotion", this.attackerMotion);
		config.options().copyDefaults(true);
		Finale.getPlugin().saveConfig();
	}

	public double getKnockbackLevelMultiplier() {
		return knockbackLevelMultiplier;
	}

	public Vector getKnockbackMultiplier() {
		return knockbackMultiplier;
	}

	public Vector getSprintMultiplier() {
		return sprintMultiplier;
	}

	public Vector getAirKnockbackMultiplier() {
		return airKnockbackMultiplier;
	}

	public Vector getWaterKnockbackMultiplier() {
		return waterKnockbackMultiplier;
	}

	public Vector getVictimMotion() {
		return victimMotion;
	}

	public Vector getMaxVictimMotion() {
		return maxVictimMotion;
	}

	public Vector getAttackerMotion() {
		return attackerMotion;
	}

	public boolean isSprintResetEnabled() {
		return sprintResetEnabled;
	}

	public boolean isWaterSprintResetEnabled() {
		return waterSprintResetEnabled;
	}

	public int getCPSLimit() {
		return cpsLimit;
	}

	public long getCpsCounterInterval() {
		return cpsCounterInterval;
	}

	public boolean isKnockbackSwordsEnabled() {
		return knockbackSwordsEnabled;
	}

	public boolean isAttackCooldownEnabled() {
		return attackCooldownEnabled;
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

	public void setKnockbackMultiplier(Vector knockbackMultiplier) {
		this.knockbackMultiplier = knockbackMultiplier;
	}

	public void setSprintMultiplier(Vector sprintMultiplier) {
		this.sprintMultiplier = sprintMultiplier;
	}

	public void setAirKnockbackMultiplier(Vector airKnockbackMultiplier) {
		this.airKnockbackMultiplier = airKnockbackMultiplier;
	}

	public void setWaterKnockbackMultiplier(Vector waterKnockbackMultiplier) {
		this.waterKnockbackMultiplier = waterKnockbackMultiplier;
	}

	public void setVictimMotion(Vector victimMotion) {
		this.victimMotion = victimMotion;
	}

	public void setMaxVictimMotion(Vector maxVictimMotion) {
		this.maxVictimMotion = maxVictimMotion;
	}

	public void setAttackerMotion(Vector attackerMotion) {
		this.attackerMotion = attackerMotion;
	}

	public void setSprintResetEnabled(boolean sprintResetEnabled) {
		this.sprintResetEnabled = sprintResetEnabled;
	}

	public void setWaterSprintResetEnabled(boolean waterSprintResetEnabled) {
		this.waterSprintResetEnabled = waterSprintResetEnabled;
	}

	public void setKnockbackSwordsEnabled(boolean knockbackSwordsEnabled) {
		this.knockbackSwordsEnabled = knockbackSwordsEnabled;
	}
}
