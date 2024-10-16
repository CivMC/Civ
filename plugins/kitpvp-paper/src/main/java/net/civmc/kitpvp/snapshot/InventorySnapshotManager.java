package net.civmc.kitpvp.snapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventorySnapshotManager {

    private final Map<UUID, InventorySnapshot> snapshots = new HashMap<>();

    public InventorySnapshot getSnapshot(UUID player) {
        return this.snapshots.get(player);
    }

    public void putSnapshot(UUID player, InventorySnapshot snapshot) {
        this.snapshots.put(player, snapshot);
    }
}
