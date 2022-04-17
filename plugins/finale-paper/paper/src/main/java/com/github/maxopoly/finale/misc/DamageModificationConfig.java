package com.github.maxopoly.finale.misc;

public class DamageModificationConfig {

	public enum Type {
		ALL, SWORD, SHARPNESS_ENCHANT, STRENGTH_EFFECT, ARROW, POWER_ENCHANT, CRIT, TRIDENT, IMPALE_ENCHANT, FIREWORK
	}

	private double multiplier;
	private double flatAddition;
	private MultiplierMode mode;
	private Type type;

	public DamageModificationConfig(Type type, MultiplierMode mode, double multiplier, double flatAddition) {
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
		double multi = mode.apply(damage, multiplier, level);
		//System.out.println("damage: " + damage + " multiplier: " + multi);
		return damage * multi;
	}

}
