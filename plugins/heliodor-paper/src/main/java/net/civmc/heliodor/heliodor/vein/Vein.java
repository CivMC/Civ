package net.civmc.heliodor.heliodor.vein;

public record Vein(
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
    int blocksAvailableEstimate,
    int blocksMined,
    boolean discovered,
    int ores
) {
}
