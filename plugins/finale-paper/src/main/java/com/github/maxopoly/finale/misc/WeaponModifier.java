package com.github.maxopoly.finale.misc;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;

public class WeaponModifier {

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

	private Map<Material, WeaponConfig> weapons;

	public WeaponModifier() {
		this.weapons = new HashMap<Material, WeaponModifier.WeaponConfig>();
	}

	public void addWeapon(Material m, int damage, double attackSpeed) {
		weapons.put(m, new WeaponConfig(m, damage, attackSpeed));
	}

	public double getAttackSpeed(Material m) {
		WeaponConfig config = weapons.get(m);
		if (config == null) {
			return -1.0;
		}
		return config.getAttackSpeed();
	}

	public int getDamage(Material m) {
		WeaponConfig config = weapons.get(m);
		if (config == null) {
			return -1;
		}
		return config.getDamage();
	}

}
