package com.github.maxopoly.finale.misc.knockback;

public class KnockbackConfig {

	private KnockbackModifier groundModifier;
	private KnockbackModifier airModifier;
	private KnockbackModifier waterModifier;

	public KnockbackConfig(KnockbackModifier groundModifier, KnockbackModifier airModifier, KnockbackModifier waterModifier) {
		this.groundModifier = groundModifier;
		this.airModifier = airModifier;
		this.waterModifier = waterModifier;
	}

	public KnockbackModifier getGroundModifier() {
		return groundModifier;
	}

	public void setGroundModifier(KnockbackModifier groundModifier) {
		this.groundModifier = groundModifier;
	}

	public KnockbackModifier getAirModifier() {
		return airModifier;
	}

	public void setAirModifier(KnockbackModifier airModifier) {
		this.airModifier = airModifier;
	}

	public KnockbackModifier getWaterModifier() {
		return waterModifier;
	}

	public void setWaterModifier(KnockbackModifier waterModifier) {
		this.waterModifier = waterModifier;
	}

	@Override
	public String toString() {
		return "KnockbackConfig{" +
				"groundModifier=" + groundModifier +
				", airModifier=" + airModifier +
				", waterModifier=" + waterModifier +
				'}';
	}
}
