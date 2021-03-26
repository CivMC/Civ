package com.github.maxopoly.finale.misc;

import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Material;

public class WeaponModifier {
	
	public static final int DAMAGE_NON_ADJUSTED = -1;
	public static final double ATTACK_SPEED_NON_ADJUSTED = -1.0D;

	private static final class WeaponConfig {
		private Material mat;
		private double damage;
		private double attackSpeed;

		private WeaponConfig(Material mat, double damage, double attackSpeed) {
			this.mat = mat;
			this.damage = damage;
			this.attackSpeed = attackSpeed;
		}

		public double getAttackSpeed() {
			return attackSpeed;
		}

		public double getDamage() {
			return damage;
		}

		public Material getMaterial() {
			return mat;
		}
	}

	private Map<Material, WeaponConfig> weapons;

	public WeaponModifier() {
		this.weapons = new EnumMap<>(Material.class);
	}

	public void addWeapon(Material m, int damage, double attackSpeed) {
		weapons.put(m, new WeaponConfig(m, damage, attackSpeed));
	}

	public double getAttackSpeed(Material m) {
		WeaponConfig config = weapons.get(m);
		if (config == null) {
			return DAMAGE_NON_ADJUSTED;
		}
		return config.getAttackSpeed();
	}

	public double getDamage(Material m) {
		WeaponConfig config = weapons.get(m);
		if (config == null) {
			return ATTACK_SPEED_NON_ADJUSTED;
		}
		return config.getDamage();
	}

}
