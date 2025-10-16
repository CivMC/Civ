package sh.okx.railswitch.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Represents the stored configuration for a detector rail switch.
 */
public final class RailSwitchRecord {

    private final UUID worldId;
    private final int x;
    private final int y;
    private final int z;
    private final String header;
    private final List<String> lines;

    public RailSwitchRecord(UUID worldId, int x, int y, int z, String header, List<String> lines) {
        this.worldId = worldId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.header = header;
        this.lines = lines == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(lines));
    }

    public RailSwitchRecord(RailSwitchKey key, String header, List<String> lines) {
        this(key.getWorldId(), key.getX(), key.getY(), key.getZ(), header, lines);
    }

    public UUID getWorldId() {
        return worldId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getHeader() {
        return header;
    }

    public List<String> getLines() {
        return lines;
    }

    public Location toLocation() {
        World world = Bukkit.getWorld(worldId);
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z);
    }

    public RailSwitchKey toKey() {
        return RailSwitchKey.of(worldId, x, y, z);
    }
}
