package net.civmc.kitpvp.kit;

import org.bukkit.potion.PotionType;

public enum KitPotion {
    NIGHT_VISION(PotionType.NIGHT_VISION, 0),
    LONG_NIGHT_VISION(PotionType.LONG_NIGHT_VISION, 0),
    INVISIBILITY(PotionType.INVISIBILITY, 0),
    LONG_INVISIBILITY(PotionType.LONG_INVISIBILITY, 0),
    LEAPING(PotionType.LEAPING, 0),
    LONG_LEAPING(PotionType.LONG_LEAPING, 0),
    STRONG_LEAPING(PotionType.STRONG_LEAPING, 0),
    FIRE_RESISTANCE(PotionType.FIRE_RESISTANCE, 0),
    LONG_FIRE_RESISTANCE(PotionType.LONG_FIRE_RESISTANCE, 0),
    SWIFTNESS(PotionType.SWIFTNESS, 0),
    LONG_SWIFTNESS(PotionType.LONG_SWIFTNESS, 0),
    STRONG_SWIFTNESS(PotionType.STRONG_SWIFTNESS, 0),
    SLOWNESS(PotionType.SLOWNESS, 1),
    LONG_SLOWNESS(PotionType.LONG_SLOWNESS, 2),
    STRONG_SLOWNESS(PotionType.STRONG_SLOWNESS, 2),
    WATER_BREATHING(PotionType.WATER_BREATHING, 0),
    LONG_WATER_BREATHING(PotionType.LONG_WATER_BREATHING, 0),
    HEALING(PotionType.HEALING, 0),
    STRONG_HEALING(PotionType.STRONG_HEALING, 0),
    HARMING(PotionType.HARMING, 1),
    STRONG_HARMING(PotionType.STRONG_HARMING, 2),
    POISON(PotionType.POISON, 1),
    LONG_POISON(PotionType.LONG_POISON, 2),
    STRONG_POISON(PotionType.STRONG_POISON,2),
    REGENERATION(PotionType.REGENERATION, 0),
    LONG_REGENERATION(PotionType.LONG_REGENERATION, 0),
    STRONG_REGENERATION(PotionType.STRONG_REGENERATION, 0),
    STRENGTH(PotionType.STRENGTH, 0),
    LONG_STRENGTH(PotionType.LONG_STRENGTH, 0),
    STRONG_STRENGTH(PotionType.STRONG_STRENGTH, 0),
    WEAKNESS(PotionType.WEAKNESS, 1),
    LONG_WEAKNESS(PotionType.LONG_WEAKNESS, 3),
    LUCK(PotionType.LUCK, 0),
    TURTLE_MASTER(PotionType.TURTLE_MASTER, 5),
    LONG_TURTLE_MASTER(PotionType.LONG_TURTLE_MASTER, 5),
    STRONG_TURTLE_MASTER(PotionType.STRONG_TURTLE_MASTER, 5),
    SLOW_FALLING(PotionType.SLOW_FALLING, 0),
    LONG_SLOW_FALLING(PotionType.LONG_SLOW_FALLING, 0),
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
