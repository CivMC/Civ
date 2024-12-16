package net.civmc.kitpvp.spawn;

import org.bukkit.Location;

public interface SpawnProvider {
    Location getSpawn();
    boolean setSpawn(Location location);
}
