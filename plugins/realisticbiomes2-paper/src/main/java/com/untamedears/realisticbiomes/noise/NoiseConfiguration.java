package com.untamedears.realisticbiomes.noise;

import org.bukkit.configuration.ConfigurationSection;

public record NoiseConfiguration(int octaves, double frequency, double amplitude, double lacunarity, double persistence,
                                 double scale, long seed) {

    public static NoiseConfiguration fromConfiguration(ConfigurationSection section) {
       return new NoiseConfiguration(
           section.getInt("octaves", 1),
           section.getDouble("frequency", 1),
           section.getDouble("amplitude", 1),
           section.getDouble("lacunarity", 2),
           section.getDouble("persistence", 2),
           section.getDouble("scale", 1),
           section.getLong("seed")
       );
    }

}
