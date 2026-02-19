package net.civmc.heliodor.vein;

import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import vg.civcraft.mc.civmodcore.utilities.BlockPosPdc;

public class OrePredicate implements Predicate<Location> {

    private final NamespacedKey oreLocationsKey;

    public OrePredicate(NamespacedKey oreLocationsKey) {
        this.oreLocationsKey = oreLocationsKey;
    }

    @Override
    public boolean test(Location location) {
        if (location.getBlock().getType() != Material.RAW_IRON_BLOCK) {
            return false;
        }
        return BlockPosPdc.testBlock(location.getChunk(), oreLocationsKey, location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
