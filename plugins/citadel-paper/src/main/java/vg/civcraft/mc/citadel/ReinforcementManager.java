package vg.civcraft.mc.citadel;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.citadel.model.CitadelChunkData;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.api.BlockBasedChunkMetaView;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableBasedDataObject;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableStorageEngine;

public class ReinforcementManager {

    @Nullable
    private final BlockBasedChunkMetaView<CitadelChunkData, TableBasedDataObject, TableStorageEngine<Reinforcement>> chunkMetaData;
    private final Map<MemoryOnlyReinforcementKey, Reinforcement> memoryOnlyReinforcements;

    ReinforcementManager(
        @Nullable final BlockBasedChunkMetaView<CitadelChunkData, TableBasedDataObject, TableStorageEngine<Reinforcement>> chunkMetaData) {
        this.chunkMetaData = chunkMetaData;
        this.memoryOnlyReinforcements = new ConcurrentHashMap<>();
    }

    /**
     * Gets the reinforcement at the given location if one exists
     *
     * @param location Location to get reinforcement for
     * @return Reinforcement at the location or null if no reinforcement exists
     * there
     */
    public Reinforcement getReinforcement(final Location location) {
        if (isMemoryOnlyWorld(location.getWorld())) {
            return memoryOnlyReinforcements.get(MemoryOnlyReinforcementKey.from(location));
        }
        if (chunkMetaData == null) {
            return null;
        }
        return (Reinforcement) chunkMetaData.get(location);
    }

    /**
     * Gets the reinforcement for the given block if one exists
     *
     * @param block Block to get reinforcement for
     * @return Reinforcement for the block or null if no reinforcement exists there
     */
    public Reinforcement getReinforcement(final Block block) {
        return getReinforcement(block.getLocation());
    }

    /**
     * Inserts the given reinforcement into the tracking. If a reinforcement already
     * exists at the same location it will be replaced.
     *
     * @param reinforcement Reinforcement to insert
     */
    public void putReinforcement(final Reinforcement reinforcement) {
        if (isMemoryOnlyWorld(reinforcement.getLocation().getWorld())) {
            memoryOnlyReinforcements.put(MemoryOnlyReinforcementKey.from(reinforcement.getLocation()), reinforcement);
            return;
        }
        if (chunkMetaData == null) {
            return;
        }
        chunkMetaData.put(reinforcement);
    }

    public void removeReinforcement(final Reinforcement reinforcement) {
        final Location location = reinforcement.getLocation();
        if (isMemoryOnlyWorld(location.getWorld())) {
            memoryOnlyReinforcements.remove(MemoryOnlyReinforcementKey.from(location));
        }
    }

    public void removeMemoryOnlyReinforcements(final World world) {
        if (!isMemoryOnlyWorld(world)) {
            return;
        }
        final UUID worldId = world.getUID();
        memoryOnlyReinforcements.keySet().removeIf(key -> key.worldId().equals(worldId));
    }

    void shutDown() {
        if (chunkMetaData != null) {
            chunkMetaData.disable();
        }
    }

    private static boolean isMemoryOnlyWorld(final World world) {
        return Citadel.getInstance().getConfigManager().isMemoryOnlyWorld(world);
    }

    private record MemoryOnlyReinforcementKey(UUID worldId, int x, int y, int z) {

        private static MemoryOnlyReinforcementKey from(final Location location) {
            return new MemoryOnlyReinforcementKey(location.getWorld().getUID(), location.getBlockX(),
                location.getBlockY(), location.getBlockZ());
        }
    }
}
