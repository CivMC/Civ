package com.untamedears.realisticbiomes;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Beacon;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.persistence.PersistentDataType;

public class FarmBeaconManager {

    private final NamespacedKey farmBeaconKey;
    private final int beaconRange;
    private final double farmBeaconFertility;
    private final double farmBeaconGrowthMultiplier;
    private final long farmBeaconWarmup;

    public FarmBeaconManager(NamespacedKey farmBeaconKey, int beaconRange, double farmBeaconFertility, double farmBeaconGrowthMultiplier, long farmBeaconWarmup) {
        this.farmBeaconKey = farmBeaconKey;
        this.beaconRange = beaconRange;
        this.farmBeaconFertility = farmBeaconFertility;
        this.farmBeaconGrowthMultiplier = farmBeaconGrowthMultiplier;
        this.farmBeaconWarmup = farmBeaconWarmup;
    }

    public static FarmBeaconManager fromConfiguration(ConfigurationSection section) {
        return new FarmBeaconManager(
            NamespacedKey.fromString(section.getString("farm_beacon_key")),
            section.getInt("farm_beacon_range"),
            section.getDouble("farm_beacon_fertility"),
            section.getDouble("farm_beacon_growth_multiplier"),
            section.getLong("farm_beacon_warmup")
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

                    if (beacon.getTier() <= 0) {
                        continue;
                    }

                    Long value = beacon.getPersistentDataContainer().get(farmBeaconKey, PersistentDataType.LONG);
                    if (value == null) {
                        continue;
                    }

                    if (System.currentTimeMillis() - value >= farmBeaconWarmup) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public double getFarmBeaconGrowthMultiplier() {
        return farmBeaconGrowthMultiplier;
    }

    public double getFarmBeaconFertility() {
        return farmBeaconFertility;
    }
}
