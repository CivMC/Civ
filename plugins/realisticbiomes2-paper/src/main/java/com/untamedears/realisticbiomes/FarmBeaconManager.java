package com.untamedears.realisticbiomes;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;

public class FarmBeaconManager {

    private final NamespacedKey farmBeaconKey;
    private final int beaconRange;
    private final double farmBeaconFertility;

    public FarmBeaconManager(NamespacedKey farmBeaconKey, int beaconRange, double farmBeaconFertility) {
        this.farmBeaconKey = farmBeaconKey;
        this.beaconRange = beaconRange;
        this.farmBeaconFertility = farmBeaconFertility;
    }

    public static FarmBeaconManager fromConfiguration(ConfigurationSection section) {
        return new FarmBeaconManager(
            NamespacedKey.fromString(section.getString("farm_beacon_key")),
            section.getInt("farm_beacon_range"),
            section.getDouble("farm_beacon_fertility")
        );
    }

    public boolean isNearBeacon(Location location) {
        World world = location.getWorld();

        int chunkX = location.getChunk().getX();
        int chunkZ = location.getChunk().getZ();

        int chunkRange = Math.ceilDiv(beaconRange, 16);
        for (int x = -chunkRange; x <= chunkRange; x++) {
            for (int z = -chunkRange; z <= chunkRange; z++) {
                Chunk nearbyChunk = world.getChunkAt(x + chunkX, z + chunkZ);
                for (BlockState state : nearbyChunk.getTileEntities(b -> b.getType() == Material.BEACON, false)) {
                    int blockX = state.getX();
                    int blockY = state.getY();
                    int blockZ = state.getZ();

                    if ((location.getX() - blockX) * (location.getX() - blockX) + (location.getY() - blockY) * (location.getY() - blockY) + (location.getZ() - blockZ) * (location.getZ() - blockZ) > this.beaconRange * this.beaconRange) {
                        continue;
                    }

                    if (!(state instanceof Beacon beacon)) {
                        continue;
                    }

                    if (beacon.getTier() > 0 && beacon.getPersistentDataContainer().has(farmBeaconKey)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public double getFarmBeaconFertility() {
        return farmBeaconFertility;
    }
}
