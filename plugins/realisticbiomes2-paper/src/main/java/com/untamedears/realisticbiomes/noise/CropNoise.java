package com.untamedears.realisticbiomes.noise;

import org.bukkit.configuration.ConfigurationSection;

public class CropNoise {

    private static final double SCALE = 64;

    private final SimplexNoise temperatureNoise;
    private final NoiseConfiguration temperatureConfiguration;
    private final SimplexNoise humidityNoise;
    private final NoiseConfiguration humidityConfiguration;
    private final SimplexNoise fertilitySeed;
    private final SimplexNoise fertilitySeed2;

    private final double yieldPowerFactor;

    private final double fertilityPower;
    private final double fertilityScale;

    private final double fertilityScale2;
    private final double fertilityMul2;

    private CropNoise(NoiseConfiguration temperature, NoiseConfiguration humidity, long fertilitySeed, long fertilitySeed2, double fertilityScale2, double fertilityMul2, double yieldPowerFactor, double fertilityPower, double fertilityScale) {
        this.temperatureNoise = new SimplexNoise(temperature.seed());
        this.temperatureConfiguration = temperature;
        this.humidityNoise = new SimplexNoise(humidity.seed());
        this.humidityConfiguration = humidity;
        this.fertilitySeed = new SimplexNoise(fertilitySeed);
        this.fertilitySeed2 = new SimplexNoise(fertilitySeed2);
        this.fertilityScale2 = fertilityScale2;
        this.fertilityMul2 = fertilityMul2;
        this.yieldPowerFactor = yieldPowerFactor;
        this.fertilityPower = fertilityPower;
        this.fertilityScale = fertilityScale;
    }

    public static CropNoise fromConfiguration(ConfigurationSection section) {
        return new CropNoise(
            NoiseConfiguration.fromConfiguration(section.getConfigurationSection("temperature")),
            NoiseConfiguration.fromConfiguration(section.getConfigurationSection("humidity")),
            section.getLong("fertility_seed"),
            section.getLong("fertility_seed2"),
            section.getDouble("fertility2_scale"),
            section.getDouble("fertility2_mul"),
            section.getDouble("yield_power_factor"),
            section.getDouble("fertility_power"),
            section.getDouble("fertility_scale")
        );
    }

    public double getFertility(int x, int z) {
        return Math.pow((Math.max(this.fertilitySeed2.noise(x / fertilityScale2, z / fertilityScale2) * fertilityMul2, this.fertilitySeed.noise(x / fertilityScale, z / fertilityScale)) + 1) / 2, this.fertilityPower);
    }

    public double getHumidity(int x, int z) {
        return this.humidityConfiguration.scale() * (this.humidityNoise.fractal(this.humidityConfiguration.octaves(),
            x / SCALE,
            z / SCALE,
            this.humidityConfiguration.frequency(),
            this.humidityConfiguration.amplitude(),
            this.humidityConfiguration.lacunarity(),
            this.humidityConfiguration.persistence()));
    }

    public double getHumidityScale() {
        return this.humidityConfiguration.scale();
    }

    public double getTemperature(int x, int z) {
        return this.temperatureConfiguration.scale() * (this.temperatureNoise.fractal(this.temperatureConfiguration.octaves(),
            x / SCALE,
            z / SCALE,
            this.temperatureConfiguration.frequency(),
            this.temperatureConfiguration.amplitude(),
            this.temperatureConfiguration.lacunarity(),
            this.temperatureConfiguration.persistence()));
    }

    public double getYield(int x, int z, double biomeTemperature, double biomeHumidity, double cropTemperature, double cropHumidity, double extraFertility) {
        double temperature = getTemperature(x, z) + biomeTemperature;
        double humidity = getHumidity(x, z) + biomeHumidity;
        double fertility = getFertility(x, z) + extraFertility;

        double dt = temperature - cropTemperature;
        double dh = humidity - cropHumidity;

        return Math.pow(fertility * (1 - Math.min(1, Math.sqrt(dt * dt + dh * dh))), this.yieldPowerFactor);
    }
}
