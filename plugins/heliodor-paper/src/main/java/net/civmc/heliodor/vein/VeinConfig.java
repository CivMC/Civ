package net.civmc.heliodor.vein;

public record VeinConfig(
    String world,
    int frequencyMinutes,
    int spawnRadius,
    int minOre,
    int maxOre,
    int lowDistance,
    int highDistance,
    int inaccuracy,
    int maxSpawns,
    int minBlocks) {

}
