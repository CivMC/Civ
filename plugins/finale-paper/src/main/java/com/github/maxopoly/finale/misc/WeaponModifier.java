package com.github.maxopoly.finale.misc;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;

public class WeaponModifier {
	
	public static final int DAMAGE_NON_ADJUSTED = -1;
	public static final double ATTACK_SPEED_NON_ADJUSTED = -1.0d;

	private class WeaponConfig {
		private Material mat;
		private int damage;
		private double attackSpeed;

		private WeaponConfig(Material mat, int damage, double attackSpeed) {
			this.mat = mat;
			this.damage = damage;
			this.attackSpeed = attackSpeed;
		}

		public double getAttackSpeed() {
			return attackSpeed;
		}

		public int getDamage() {
			return damage;
		}

		public Material getMaterial() {
			return mat;
		}
	}

	private Map<Material, WeaponConfig> weapons = new HashMap<>();

	public void addWeapon(Material m, int damage, double attackSpeed) {
		weapons.put(m, new WeaponConfig(m, damage, attackSpeed));
	}

	public int getDamage(Material m) {
		WeaponConfig config = weapons.get(m);
		if (config == null) {
			return DAMAGE_NON_ADJUSTED;
		}
		return config.getDamage();
	}

	public double getAttackSpeed(Material m) {
		WeaponConfig config = weapons.get(m);
		if (config == null) {
			return ATTACK_SPEED_NON_ADJUSTED;
		}
		return config.getAttackSpeed();
	}

}
