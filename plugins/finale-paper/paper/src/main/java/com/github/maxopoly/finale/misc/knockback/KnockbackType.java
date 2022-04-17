package com.github.maxopoly.finale.misc.knockback;

public enum KnockbackType {

	ADD((start, knockback, modifier) -> knockback.clone().add(modifier)),
	MULTIPLY((start, knockback, modifier) -> knockback.clone().multiply(modifier));

	private KnockbackModifier.KnockbackLogic modifierLogic;

	KnockbackType(KnockbackModifier.KnockbackLogic modifierLogic) {
		this.modifierLogic = modifierLogic;
	}

	public KnockbackModifier.KnockbackLogic getModifierLogic() {
		return modifierLogic;
	}
}
