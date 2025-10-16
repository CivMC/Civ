package sh.okx.railswitch.storage;

import java.util.Objects;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

/**
 * Represents the coordinate of a detector rail switch.
 */
public final class RailSwitchKey {

    private final UUID worldId;
    private final int x;
    private final int y;
    private final int z;

    private RailSwitchKey(UUID worldId, int x, int y, int z) {
        this.worldId = worldId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static RailSwitchKey from(Block block) {
        return from(block.getLocation());
    }

    public static RailSwitchKey from(BlockState state) {
        return from(state.getLocation());
    }

    public static RailSwitchKey from(Location location) {
        if (location == null || location.getWorld() == null) {
            throw new IllegalArgumentException("Location or world cannot be null.");
        }
        return new RailSwitchKey(location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static RailSwitchKey of(UUID worldId, int x, int y, int z) {
        if (worldId == null) {
            throw new IllegalArgumentException("WorldId cannot be null.");
        }
        return new RailSwitchKey(worldId, x, y, z);
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

    public String toPathKey() {
        return worldId + ";" + x + ";" + y + ";" + z;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RailSwitchKey)) {
            return false;
        }
        RailSwitchKey that = (RailSwitchKey) other;
        return x == that.x
            && y == that.y
            && z == that.z
            && Objects.equals(worldId, that.worldId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldId, x, y, z);
    }

    @Override
    public String toString() {
        return "RailSwitchKey{"
            + "worldId=" + worldId
            + ", x=" + x
            + ", y=" + y
            + ", z=" + z
            + '}';
    }
}
