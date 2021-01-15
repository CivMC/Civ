package com.github.maxopoly.finale.misc;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;

public class ArmourModifier {
	
	public static class ArmourConfig {

		private double toughness;
		private double armour;
		private double knockbackResistance;

		public ArmourConfig(double toughness, double armour, double knockbackResistance) {
			this.toughness = toughness;
			this.armour = armour;
			this.knockbackResistance = knockbackResistance;
		}

		public double getToughness() {
			return toughness;
		}
		
		public double getArmour() {
			return armour;
		}

		public double getKnockbackResistance() {
			return knockbackResistance;
		}

		@Override
		public String toString() {
			return "Armour [toughness=" + toughness + ", armour=" + armour + ", kb_resistance=" + knockbackResistance + "]";
		}
		
	}
	
	private Map<Material, ArmourConfig> armour;

	public ArmourModifier() {
		this.armour = new HashMap<Material, ArmourConfig>();
	}

	public void addArmour(Material m, double toughness, double armour, double knockbackResistance) {
		this.armour.put(m, new ArmourConfig(toughness, armour, knockbackResistance));
	}

	public double getToughness(Material m) {
		ArmourConfig config = armour.get(m);
		if (config == null) {
			return -1;
		}
		return config.getToughness();
	}

	public double getArmour(Material m) {
		ArmourConfig config = armour.get(m);
		if (config == null) {
			return -1;
		}
		return config.getArmour();
	}

	public double getKnockbackResistance(Material m) {
		ArmourConfig config = armour.get(m);
		if (config == null) {
			return -1;
		}
		return config.getKnockbackResistance();
	}
	
}
