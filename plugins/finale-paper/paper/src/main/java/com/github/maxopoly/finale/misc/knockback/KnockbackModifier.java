package com.github.maxopoly.finale.misc.knockback;

import org.bukkit.util.Vector;

public class KnockbackModifier {

	public interface KnockbackLogic {

		Vector apply(Vector start, Vector current, Vector modifier);

	}

	private KnockbackType type;
	private Vector modifier;

	public KnockbackModifier(KnockbackType type, Vector modifier) {
		this.type = type;
		this.modifier = modifier;
	}

	public Vector modifyKnockback(Vector start, Vector knockback) {
		return type.getModifierLogic().apply(start, knockback, modifier);
	}

	public KnockbackType getType() {
		return type;
	}

	public Vector getModifier() {
		return modifier;
	}

	@Override
	public String toString() {
		return "KnockbackModifier{" +
				"type=" + type +
				", modifier=" + modifier +
				'}';
	}
}
