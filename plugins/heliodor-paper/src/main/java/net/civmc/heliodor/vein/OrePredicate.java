package net.civmc.heliodor.vein;

import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class OrePredicate implements Predicate<Location> {

    private final NamespacedKey oreLocationsKey;

    public OrePredicate(NamespacedKey oreLocationsKey) {
        this.oreLocationsKey = oreLocationsKey;
    }

    @Override
    public boolean test(Location location) {
        PersistentDataContainer chunkPdc = location.getChunk().getPersistentDataContainer();
        int[] ints = chunkPdc.get(oreLocationsKey, PersistentDataType.INTEGER_ARRAY);
        if (ints == null) {
            return false;
        }

        for (int i = 0; i < ints.length; i += 3) {
            if (ints[i] == location.getBlockX() && ints[i + 1] == location.getBlockY() && ints[i + 2] == location.getBlockZ()) {
                return true;
            }
        }
        return false;
    }
}
