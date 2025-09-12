package com.untamedears.realisticbiomes.noise;

import com.untamedears.realisticbiomes.FarmBeaconManager;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BiomeConfiguration {

    private final CropNoise noise;
    private final FarmBeaconManager farmBeaconManager;
    private final Map<Biome, Climate> biomeClimates;
    private double yieldOverride = -1;

    private BiomeConfiguration(CropNoise noise, FarmBeaconManager farmBeaconManager, Map<Biome, Climate> biomeClimates) {
        this.noise = noise;
        this.farmBeaconManager = farmBeaconManager;
        this.biomeClimates = biomeClimates;
    }

    @SuppressWarnings("unchecked")
    public static BiomeConfiguration fromConfiguration(CropNoise noise, FarmBeaconManager farmBeaconManager, List<Map<?, ?>> climatesList, Map<String, List<Biome>> biomeAliases) {
        Map<Biome, Climate> biomeClimates = new HashMap<>();
        for (Map<?, ?> climateMap : climatesList) {
            Climate climate = new Climate(((Number) climateMap.get("temperature")).doubleValue(), ((Number) climateMap.get("humidity")).doubleValue(), (boolean) ((Map<Object, Object>) climateMap).getOrDefault("saline", false), (boolean) ((Map<Object, Object>) climateMap).getOrDefault("hell", false));

            List<String> biomes = (List<String>) climateMap.get("biomes");
            for (String biome : biomes) {
                List<Biome> aliases = biomeAliases.get(biome);
                if (aliases == null) {
                    biomeClimates.put(RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME).get(NamespacedKey.minecraft(biome.toLowerCase())), climate);
                } else {
                    for (Biome alias : aliases) {
                        biomeClimates.put(alias, climate);
                    }
                }
            }
        }
        return new BiomeConfiguration(noise, farmBeaconManager, biomeClimates);
    }

    public Climate getClimate(Biome biome) {
        return biomeClimates.get(biome);
    }

    public double getFertility(Block block) {
        return noise.getFertility(block.getX(), block.getZ());
    }

    public double getTemperature(Block block) {
        return noise.getTemperature(block.getX(), block.getZ());
    }

    public double getHumidity(Block block) {
        return noise.getHumidity(block.getX(), block.getZ());
    }

    public double getHumidityScale() {
        return noise.getHumidityScale();
    }

    public void setYieldOverride(double yieldOverride) {
        this.yieldOverride = yieldOverride;
    }

    public double getYield(Block block, Climate climate, Material mat, int maxYield) {
        if (yieldOverride != -1) {
            return yieldOverride;
        }
        Climate biomeClimate = biomeClimates.get(block.getBiome());
        if (biomeClimate == null || biomeClimate.saline() != climate.saline() || biomeClimate.hell() != climate.hell()) {
            return 0;
        }
        double yield = noise.getYield(block.getX(), block.getZ(), biomeClimate.temperature(), biomeClimate.humidity(), climate.temperature(), climate.humidity(),
            farmBeaconManager.isNearBeacon(block.getLocation()) ? farmBeaconManager.getFarmBeaconFertility() : 0);
        if (mat == Material.WHEAT && yield < 1.0 / maxYield) {
            yield = (1.00001 / maxYield);
        }
        return yield;
    }
}
