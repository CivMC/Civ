package com.github.maxopoly.finale;

import com.github.maxopoly.finale.misc.SaturationHealthRegenHandler;
import com.github.maxopoly.finale.misc.WeaponModifier;
import com.github.maxopoly.finale.potion.PotionHandler;

public class FinaleManager {

	private boolean debug;
	private boolean attackSpeedEnabled;
	private double attackSpeed;
	private boolean regenHandlerEnabled;
	private int invulnerableTicks;
	private SaturationHealthRegenHandler regenHandler;
	private WeaponModifier weaponModifier;
	private PotionHandler potionHandler;

	public FinaleManager(boolean debug, boolean attackSpeedEnabled, double attackSpeed, int invulnerableTicks,
			boolean regenHandlerEnabled, SaturationHealthRegenHandler regenHandler, WeaponModifier weaponModifier,
			PotionHandler potionHandler) {
		this.attackSpeedEnabled = attackSpeedEnabled;
		this.attackSpeed = attackSpeed;
		this.regenHandlerEnabled = regenHandlerEnabled;
		this.regenHandler = regenHandler;
		this.weaponModifier = weaponModifier;
		this.potionHandler = potionHandler;
		this.invulnerableTicks = invulnerableTicks;
	}

	public double getAttackSpeed() {
		return attackSpeed;
	}

	public SaturationHealthRegenHandler getPassiveRegenHandler() {
		return regenHandler;
	}

	public PotionHandler getPotionHandler() {
		return potionHandler;
	}

	public WeaponModifier getWeaponModifer() {
		return weaponModifier;
	}

	public boolean isAttackSpeedEnabled() {
		return attackSpeedEnabled;
	}

	public int getInvulnerableTicks() {
		return invulnerableTicks;
	}

	public boolean isDebug() {
		return debug;
	}

	public boolean isRegenHandlerEnabled() {
		return regenHandlerEnabled;
	}
}
