package com.github.maxopoly.finale.misc;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;

public class ArmourModifier {
	
	public static class ArmourConfig {

		private double toughness;
		private double armour;

		public ArmourConfig(double toughness, double armour) {
			this.toughness = toughness;
			this.armour = armour;
		}

		public double getToughness() {
			return toughness;
		}
		
		public double getArmour() {
			return armour;
		}
		
		@Override
		public String toString() {
			return "Armour [toughness=" + toughness + ", armour=" + armour + "]";
		}
		
	}
	
	private Map<Material, ArmourConfig> armour;

	public ArmourModifier() {
		this.armour = new HashMap<Material, ArmourConfig>();
	}

	public void addArmour(Material m, double toughness, double armour) {
		this.armour.put(m, new ArmourConfig(toughness, armour));
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

	
}
