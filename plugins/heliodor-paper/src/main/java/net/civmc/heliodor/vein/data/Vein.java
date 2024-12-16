package net.civmc.heliodor.vein.data;

public record Vein(
    int id,
    String type,
    long spawnedAt,
    String world,
    int radius,
    int x,
    int y,
    int z,
    int offsetX,
    int offsetY,
    int offsetZ,
    // estimate of the blocks available when this vein was scanned for generation
    // there may be mess than that when the vein is found, but that's fine,
    // it just means the amount of ore will be slightly lower
    // is reduced for every block mined
    int blocksAvailable,
    boolean discovered,
    int ores,
    int oresRemaining
) {
}
