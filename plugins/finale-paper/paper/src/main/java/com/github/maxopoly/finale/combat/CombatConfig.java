package com.github.maxopoly.finale.combat;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.combat.knockback.KnockbackStrategy;
import com.github.maxopoly.finale.misc.knockback.KnockbackConfig;
import com.github.maxopoly.finale.misc.knockback.KnockbackModifier;
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
	private KnockbackConfig normalConfig;
	private KnockbackConfig sprintConfig;
	private Vector victimMotion;
	private Vector maxVictimMotion;
	private Vector attackerMotion;
	private KnockbackStrategy knockbackStrategy;

	public CombatConfig(boolean attackCooldownEnabled, boolean knockbackSwordsEnabled, boolean sprintResetEnabled, boolean waterSprintResetEnabled, int cpsLimit, long cpsCounterInterval, double maxReach, boolean sweepEnabled, CombatSoundConfig combatSounds,
						double knockbackLevelMultiplier, KnockbackConfig normalConfig, KnockbackConfig sprintConfig, Vector victimMotion, Vector maxVictimMotion,
						Vector attackerMotion, KnockbackStrategy knockbackStrategy) {
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
		this.normalConfig = normalConfig;
		this.sprintConfig = sprintConfig;
		this.victimMotion = victimMotion;
		this.maxVictimMotion = maxVictimMotion;
		this.attackerMotion = attackerMotion;
		this.knockbackStrategy = knockbackStrategy;
	}

	private void setKnockbackConfig(FileConfiguration config, String name, KnockbackConfig knockbackConfig) {
		setKnockbackModifier(config, "cleanerCombat." + name + ".groundModifier", knockbackConfig.getGroundModifier());
		setKnockbackModifier(config, "cleanerCombat." + name + ".airModifier", knockbackConfig.getAirModifier());
		setKnockbackModifier(config, "cleanerCombat." + name + ".waterModifier", knockbackConfig.getWaterModifier());
	}

	private void setKnockbackModifier(FileConfiguration config, String name, KnockbackModifier modifier) {
		config.set(name + ".type", modifier.getType().toString());
		config.set(name + ".x", modifier.getModifier().getX());
		config.set(name + ".y", modifier.getModifier().getY());
		config.set(name + ".z", modifier.getModifier().getZ());
	}

	private void setVector(FileConfiguration config, String name, Vector vec) {
		config.set(name + ".x", vec.getX());
		config.set(name + ".y", vec.getY());
		config.set(name + ".z", vec.getZ());
	}

	public void save() {
		FileConfiguration config = Finale.getPlugin().getConfig();
		setKnockbackConfig(config, "normal", normalConfig);
		setKnockbackConfig(config, "sprint", sprintConfig);
		setVector(config, "cleanerCombat.victimMotion", this.victimMotion);
		setVector(config, "cleanerCombat.maxVictimMotion", this.maxVictimMotion);
		setVector(config, "cleanerCombat.attackerMotion", this.attackerMotion);
		config.options().copyDefaults(true);
		Finale.getPlugin().saveConfig();
	}

	public double getKnockbackLevelMultiplier() {
		return knockbackLevelMultiplier;
	}

	public KnockbackConfig getNormalConfig() {
		return normalConfig;
	}

	public KnockbackConfig getSprintConfig() {
		return sprintConfig;
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

	public KnockbackStrategy getKnockbackStrategy() {
		return knockbackStrategy;
	}

	public void setNormalConfig(KnockbackConfig normalConfig) {
		this.normalConfig = normalConfig;
	}

	public void setSprintConfig(KnockbackConfig sprintConfig) {
		this.sprintConfig = sprintConfig;
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
