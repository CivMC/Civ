package com.github.maxopoly.finale.misc;

public class DamageModificationConfig {

	public enum Mode {
		LINEAR, EXPONENTIAL;
	}

	public enum Type {
		ALL, SWORD, SHARPNESS_ENCHANT, STRENGTH_EFFECT, ARROW, POWER_ENCHANT
	}

	private double multiplier;
	private double flatAddition;
	private Mode mode;
	private Type type;

	public DamageModificationConfig(Type type, Mode mode, double multiplier, double flatAddition) {
		this.type = type;
		this.mode = mode;
		this.multiplier = multiplier;
		this.flatAddition = flatAddition;
	}

	public Type getType() {
		return type;
	}

	public double modify(double damage) {
		return modify(damage, 1);
	}

	public double modify(double damage, int level) {
		damage += level * flatAddition;
		switch (mode) {
		case LINEAR:
			return damage * (1.0 + ((multiplier - 1.0) * level));
		case EXPONENTIAL:
			return damage * Math.pow(multiplier, level);
		}
		throw new IllegalStateException("Mode can not be null");
	}

}
