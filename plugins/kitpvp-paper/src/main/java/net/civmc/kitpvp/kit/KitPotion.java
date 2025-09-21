package net.civmc.kitpvp.kit;

import org.bukkit.potion.PotionType;

public enum KitPotion {
    NIGHT_VISION(PotionType.NIGHT_VISION, 1),
    LONG_NIGHT_VISION(PotionType.LONG_NIGHT_VISION, 2),
    INVISIBILITY(PotionType.INVISIBILITY, 1),
    LONG_INVISIBILITY(PotionType.LONG_INVISIBILITY, 2),
    LEAPING(PotionType.LEAPING, 1),
    LONG_LEAPING(PotionType.LONG_LEAPING, 2),
    STRONG_LEAPING(PotionType.STRONG_LEAPING, 2),
    FIRE_RESISTANCE(PotionType.FIRE_RESISTANCE, 1),
    LONG_FIRE_RESISTANCE(PotionType.LONG_FIRE_RESISTANCE, 2),
    SWIFTNESS(PotionType.SWIFTNESS, 1),
    LONG_SWIFTNESS(PotionType.LONG_SWIFTNESS, 2),
    STRONG_SWIFTNESS(PotionType.STRONG_SWIFTNESS, 2),
    SLOWNESS(PotionType.SLOWNESS, 1),
    LONG_SLOWNESS(PotionType.LONG_SLOWNESS, 2),
    STRONG_SLOWNESS(PotionType.STRONG_SLOWNESS, 2),
    WATER_BREATHING(PotionType.WATER_BREATHING, 2),
    LONG_WATER_BREATHING(PotionType.LONG_WATER_BREATHING, 2),
    HEALING(PotionType.HEALING, 1),
    STRONG_HEALING(PotionType.STRONG_HEALING, 2),
    HARMING(PotionType.HARMING, 1),
    STRONG_HARMING(PotionType.STRONG_HARMING, 2),
    POISON(PotionType.POISON, 1),
    LONG_POISON(PotionType.LONG_POISON, 2),
    STRONG_POISON(PotionType.STRONG_POISON, 2),
    REGENERATION(PotionType.REGENERATION, 1),
    LONG_REGENERATION(PotionType.LONG_REGENERATION, 2),
    STRONG_REGENERATION(PotionType.STRONG_REGENERATION, 2),
    STRENGTH(PotionType.STRENGTH, 1),
    LONG_STRENGTH(PotionType.LONG_STRENGTH, 2),
    STRONG_STRENGTH(PotionType.STRONG_STRENGTH, 2),
    WEAKNESS(PotionType.WEAKNESS, 1),
    LONG_WEAKNESS(PotionType.LONG_WEAKNESS, 2),
    LUCK(PotionType.LUCK, 1),
    TURTLE_MASTER(PotionType.TURTLE_MASTER, 10),
    LONG_TURTLE_MASTER(PotionType.LONG_TURTLE_MASTER, 15),
    STRONG_TURTLE_MASTER(PotionType.STRONG_TURTLE_MASTER, 15),
    SLOW_FALLING(PotionType.SLOW_FALLING, 1),
    LONG_SLOW_FALLING(PotionType.LONG_SLOW_FALLING, 1),
    ;

    private final PotionType type;
    private final int cost;

    KitPotion(PotionType type, int cost) {
        this.type = type;
        this.cost = cost;
    }

    public PotionType getType() {
        return type;
    }

    public int getCost() {
        return cost;
    }
}
