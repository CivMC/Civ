package com.github.maxopoly.finale.misc.knockback;

public enum KnockbackType {

	DIRECT((start, knockback, modifier) -> {
		return start.clone().multiply(modifier);
	}),
	ADD((start, knockback, modifier) -> {
		return knockback.clone().add(start.clone().multiply(modifier));
	}),
	MULTIPLY((start, knockback, modifier) -> {
		return knockback.clone().multiply(modifier);
	});

	private KnockbackModifier.KnockbackLogic modifierLogic;

	KnockbackType(KnockbackModifier.KnockbackLogic modifierLogic) {
		this.modifierLogic = modifierLogic;
	}

	public KnockbackModifier.KnockbackLogic getModifierLogic() {
		return modifierLogic;
	}
}
