package com.github.maxopoly.finale.misc;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;

public class ArmourModifier {
	
	public static class ArmourConfig {

		private double toughness;
		private double armour;
		private double knockbackResistance;
		private int extraDurabilityHits;

		public ArmourConfig(double toughness, double armour, double knockbackResistance) {
			this(toughness, armour, knockbackResistance, 0);
		}
		public ArmourConfig(double toughness, double armour, double knockbackResistance, int extraDurabilityHits) {
			this.toughness = toughness;
			this.armour = armour;
			this.knockbackResistance = knockbackResistance;
			this.extraDurabilityHits = extraDurabilityHits;
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

		public int getExtraDurabilityHits() {
			return extraDurabilityHits;
		}

		@Override
		public String toString() {
			return "Armour [toughness=" + toughness + ", armour=" + armour + ", kb_resistance=" + knockbackResistance + "]";
		}
		
	}

	private ExtraDurabilityTracker extraDurabilityTracker;
	private Map<Material, ArmourConfig> armour;

	public ArmourModifier() {
		this.extraDurabilityTracker = new ExtraDurabilityTracker(this);
		this.armour = new HashMap<Material, ArmourConfig>();
	}

	public void addArmour(Material m, double toughness, double armour, double knockbackResistance, int extraDurabilityHits) {
		this.armour.put(m, new ArmourConfig(toughness, armour, knockbackResistance, extraDurabilityHits));
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

	public int getExtraDurabilityHits(Material m) {
		ArmourConfig config = armour.get(m);
		if (config == null) {
			return -1;
		}

		return config.getExtraDurabilityHits();
	}

	public ExtraDurabilityTracker getExtraDurabilityTracker() {
		return extraDurabilityTracker;
	}
}
