package vg.civcraft.mc.civmodcore.inventory.items;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public final class PotionUtils {

	private static final EnumMap<PotionType, String> POTIONS = new EnumMap<>(PotionType.class) {{
		put(PotionType.UNCRAFTABLE, "Uncraftable Potion");
		put(PotionType.WATER, "Water Bottle");
		put(PotionType.MUNDANE, "Mundane Potion");
		put(PotionType.THICK, "Thick Potion");
		put(PotionType.AWKWARD, "Awkward Potion");
		put(PotionType.FIRE_RESISTANCE, "Potion of Fire Resistance");
		put(PotionType.SPEED, "Potion of Swiftness");
		put(PotionType.SLOWNESS, "Potion of Slowness");
		put(PotionType.INSTANT_HEAL, "Potion of Healing");
		put(PotionType.INSTANT_DAMAGE, "Potion of Harming");
		put(PotionType.POISON, "Potion of Poison");
		put(PotionType.REGEN, "Potion of Regeneration");
		put(PotionType.STRENGTH, "Potion of Strength");
		put(PotionType.WEAKNESS, "Potion of Weakness");
		put(PotionType.LUCK, "Potion of Luck");
		// 1.4.2
		put(PotionType.NIGHT_VISION, "Potion of Night Vision");
		put(PotionType.INVISIBILITY, "Potion of Invisibility");
		// 1.7.2
		put(PotionType.WATER_BREATHING, "Potion of Water Breathing");
		// 1.8
		put(PotionType.JUMP, "Potion of Leaping");
		// 1.13
		put(PotionType.TURTLE_MASTER, "Potion of the Turtle Master");
		put(PotionType.SLOW_FALLING, "Potion of Slow Falling");
	}};

	private static final Map<PotionEffectType, String> EFFECTS = new HashMap<>() {{
		// Beta 1.8
		put(PotionEffectType.BLINDNESS, "Blindness");
		put(PotionEffectType.CONFUSION, "Nausea");
		put(PotionEffectType.DAMAGE_RESISTANCE, "Resistance");
		put(PotionEffectType.FAST_DIGGING, "Haste");
		put(PotionEffectType.FIRE_RESISTANCE, "Fire Resistance");
		put(PotionEffectType.HARM, "Instant Damage");
		put(PotionEffectType.HEAL, "Instant Health");
		put(PotionEffectType.HUNGER, "Hunger");
		put(PotionEffectType.INCREASE_DAMAGE, "Strength");
		put(PotionEffectType.INVISIBILITY, "Invisibility");
		put(PotionEffectType.JUMP, "Jump Boost");
		put(PotionEffectType.NIGHT_VISION, "Night Vision");
		put(PotionEffectType.POISON, "Poison");
		put(PotionEffectType.REGENERATION, "Regeneration");
		put(PotionEffectType.SLOW, "Slowness");
		put(PotionEffectType.SLOW_DIGGING, "Mining Fatigue");
		put(PotionEffectType.SPEED, "Speed");
		put(PotionEffectType.WATER_BREATHING, "Water Breathing");
		put(PotionEffectType.WEAKNESS, "Weakness");
		// 1.4.2
		put(PotionEffectType.WITHER, "Wither");
		// 1.6.1
		put(PotionEffectType.ABSORPTION, "Absorption");
		put(PotionEffectType.HEALTH_BOOST, "Health Boost");
		put(PotionEffectType.SATURATION, "Saturation");
		// 1.9
		put(PotionEffectType.GLOWING, "Glowing");
		put(PotionEffectType.LEVITATION, "Levitation");
		put(PotionEffectType.LUCK, "Luck");
		put(PotionEffectType.UNLUCK, "Bad Luck");
		// 1.13
		put(PotionEffectType.CONDUIT_POWER, "Conduit Power");
		put(PotionEffectType.DOLPHINS_GRACE, "Dolphin's Grace");
		put(PotionEffectType.SLOW_FALLING, "Slow Falling");
		// 1.14
		put(PotionEffectType.BAD_OMEN, "Bad Omen");
		put(PotionEffectType.HERO_OF_THE_VILLAGE, "Hero of the Village");
	}};

	public static void init() {
		// Determine if there's any missing potion types
		{
			final Set<PotionType> missing = new HashSet<>();
			CollectionUtils.addAll(missing, PotionType.values());
			missing.removeIf(POTIONS::containsKey);
			if (!missing.isEmpty()) {
				Bukkit.getLogger().warning("[PotionUtils] The following potion types are missing: " +
						missing.stream().map(Enum::name).collect(Collectors.joining(",")) + ".");
			}
		}
		// Determine if there's any missing potion effects
		{
			final Set<PotionEffectType> missing = new HashSet<>();
			CollectionUtils.addAll(missing, PotionEffectType.values());
			missing.removeIf(EFFECTS::containsKey);
			if (!missing.isEmpty()) {
				Bukkit.getLogger().warning("[PotionUtils] The following potion effects are missing: " +
						missing.stream().map(PotionEffectType::getName).collect(Collectors.joining(",")) + ".");
			}
		}
	}

	public static String getPotionNiceName(final PotionType potion) {
		return POTIONS.get(potion);
	}

	public static String getEffectNiceName(final PotionEffectType effect) {
		return EFFECTS.get(effect);
	}

}
