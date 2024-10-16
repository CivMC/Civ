package com.github.maxopoly.finale.combat;

public class CombatSoundConfig {

	private boolean weakEnabled;
	private boolean strongEnabled;
	private boolean knockbackEnabled;
	private boolean critEnabled;
	
	public CombatSoundConfig(boolean weakEnabled, boolean strongEnabled, boolean knockbackEnabled,
			boolean critEnabled) {
		this.weakEnabled = weakEnabled;
		this.strongEnabled = strongEnabled;
		this.knockbackEnabled = knockbackEnabled;
		this.critEnabled = critEnabled;
	}

	public boolean isWeakEnabled() {
		return weakEnabled;
	}

	public boolean isStrongEnabled() {
		return strongEnabled;
	}

	public boolean isKnockbackEnabled() {
		return knockbackEnabled;
	}

	public boolean isCritEnabled() {
		return critEnabled;
	}
	
}
