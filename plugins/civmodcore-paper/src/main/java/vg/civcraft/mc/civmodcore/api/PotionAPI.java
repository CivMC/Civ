package vg.civcraft.mc.civmodcore.api;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.potion.PotionEffectType;

public final class PotionAPI {

	private PotionAPI() {
		
	}
	
	private static Map<PotionEffectType, String> potionEffectNameMapping;
	
	
	static {
		potionEffectNameMapping = new HashMap<>();
		potionEffectNameMapping.put(PotionEffectType.ABSORPTION, "Absorption");
		potionEffectNameMapping.put(PotionEffectType.BAD_OMEN, "Bad Omen");
		potionEffectNameMapping.put(PotionEffectType.BLINDNESS, "Blindness");
		potionEffectNameMapping.put(PotionEffectType.CONDUIT_POWER, "Conduit Power");
		potionEffectNameMapping.put(PotionEffectType.CONFUSION, "Nausea");
		potionEffectNameMapping.put(PotionEffectType.DAMAGE_RESISTANCE, "Resistance");
		potionEffectNameMapping.put(PotionEffectType.DOLPHINS_GRACE, "Dolphin's Grace");
		potionEffectNameMapping.put(PotionEffectType.FAST_DIGGING, "Haste");
		potionEffectNameMapping.put(PotionEffectType.FIRE_RESISTANCE, "Fire Resistance ");
		potionEffectNameMapping.put(PotionEffectType.GLOWING, "Glowing");
		potionEffectNameMapping.put(PotionEffectType.HARM, "Instant Damage");
		potionEffectNameMapping.put(PotionEffectType.HEAL, "Instant Health");
		potionEffectNameMapping.put(PotionEffectType.HEALTH_BOOST, "Health Boost");
		potionEffectNameMapping.put(PotionEffectType.HERO_OF_THE_VILLAGE, "Hero of the Village");
		potionEffectNameMapping.put(PotionEffectType.HUNGER, "Hunger");
		potionEffectNameMapping.put(PotionEffectType.INCREASE_DAMAGE, "Strength");
		potionEffectNameMapping.put(PotionEffectType.INVISIBILITY, "Invisibility");
		potionEffectNameMapping.put(PotionEffectType.JUMP, "Jump Boost");
		potionEffectNameMapping.put(PotionEffectType.LEVITATION, "Levitation");
		potionEffectNameMapping.put(PotionEffectType.LUCK, "Luck");
		potionEffectNameMapping.put(PotionEffectType.NIGHT_VISION, "Night Vision");
		potionEffectNameMapping.put(PotionEffectType.POISON, "Poison");
		potionEffectNameMapping.put(PotionEffectType.REGENERATION, "Regeneration");
		potionEffectNameMapping.put(PotionEffectType.SATURATION, "Saturation");
		potionEffectNameMapping.put(PotionEffectType.SLOW, "Slowness");
		potionEffectNameMapping.put(PotionEffectType.SLOW_DIGGING, "Mining Fatigue");
		potionEffectNameMapping.put(PotionEffectType.SLOW_FALLING, "Slow Falling");
		potionEffectNameMapping.put(PotionEffectType.SPEED, "Speed");
		potionEffectNameMapping.put(PotionEffectType.UNLUCK, "Bad Luck");
		potionEffectNameMapping.put(PotionEffectType.WATER_BREATHING, "Haste");
		potionEffectNameMapping.put(PotionEffectType.WEAKNESS, "Weakness");
		potionEffectNameMapping.put(PotionEffectType.WITHER, "Wither");
	}
	
	public static String getNiceName(PotionEffectType pet) {
		return potionEffectNameMapping.get(pet);
	}
	
}
