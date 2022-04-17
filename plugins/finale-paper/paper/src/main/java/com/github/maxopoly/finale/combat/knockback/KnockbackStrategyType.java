package com.github.maxopoly.finale.combat.knockback;

public enum KnockbackStrategyType {

	STANDARD(new StandardKnockback());

	private KnockbackStrategy knockbackStrategy;

	KnockbackStrategyType(KnockbackStrategy knockbackStrategy) {

		this.knockbackStrategy = knockbackStrategy;
	}

	public KnockbackStrategy getKnockbackStrategy() {
		return knockbackStrategy;
	}
}
