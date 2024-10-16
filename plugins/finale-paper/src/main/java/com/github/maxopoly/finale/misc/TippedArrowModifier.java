package com.github.maxopoly.finale.misc;

import org.bukkit.Color;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.Map;

public class TippedArrowModifier {

	public enum PotionCategory {

		NORMAL,
		EXTENDED,
		AMPLIFIED;

	}

	public static class TippedArrowConfig {

		private String name;
		private PotionType potionType;
		private Color color;
		private Map<PotionCategory, Integer> durations;

		public TippedArrowConfig(String name, PotionType effectType, Color color, Map<PotionCategory, Integer> durations) {
			this.name = name;
			this.potionType = effectType;
			this.color = color;
			this.durations = durations;
		}

		public String getName() {
			return name;
		}

		public PotionType getPotionType() {
			return potionType;
		}

		public Color getColor() {
			return color;
		}

		public Map<PotionCategory, Integer> getDurations() {
			return durations;
		}

	}

	private Map<PotionType, TippedArrowConfig> tippedArrows;

	public TippedArrowModifier() {
		this.tippedArrows = new HashMap<>();
	}

	public void addTippedArrowConfig(TippedArrowConfig tippedArrowConfig) {
		tippedArrows.put(tippedArrowConfig.getPotionType(), tippedArrowConfig);
	}

	public TippedArrowConfig getTippedArrowConfig(PotionType potionType) {
		return tippedArrows.get(potionType);
	}

}
