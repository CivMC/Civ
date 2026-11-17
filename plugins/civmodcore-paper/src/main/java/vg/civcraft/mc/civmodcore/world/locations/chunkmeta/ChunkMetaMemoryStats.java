package vg.civcraft.mc.civmodcore.world.locations.chunkmeta;

import org.jetbrains.annotations.NotNull;

/**
 * Array-only estimates assuming 16-byte array headers, 4-byte references and
 * 16-byte alignment. Excludes the objects referenced by the leaf arrays.
 */
public record ChunkMetaMemoryStats(long level1Arrays, long level2Arrays, long level3Arrays,
                                   long level4Arrays, long allocatedSlots, long occupiedSlots,
                                   long estimatedBytes) {

    public static final ChunkMetaMemoryStats EMPTY = new ChunkMetaMemoryStats(0, 0, 0, 0, 0, 0, 0);

    public @NotNull ChunkMetaMemoryStats add(final @NotNull ChunkMetaMemoryStats other) {
        return new ChunkMetaMemoryStats(level1Arrays + other.level1Arrays,
            level2Arrays + other.level2Arrays, level3Arrays + other.level3Arrays,
            level4Arrays + other.level4Arrays, allocatedSlots + other.allocatedSlots,
            occupiedSlots + other.occupiedSlots, estimatedBytes + other.estimatedBytes);
    }

    public static long estimateArrayBytes(final int length) {
        return (16L + 4L * length + 15L) & ~15L;
    }
}
