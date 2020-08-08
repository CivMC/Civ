package com.untamedears.realisticbiomes;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;

import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;
import com.untamedears.realisticbiomes.growthconfig.inner.BiomeGrowthConfig;
import com.untamedears.realisticbiomes.growthconfig.inner.ChanceBasedGrowthConfig;
import com.untamedears.realisticbiomes.growthconfig.inner.PersistentGrowthConfig;

import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.CoreConfigManager;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.util.ConfigParsing;

public class RBConfigManager extends CoreConfigManager {

	private ManagedDatasource database;

	private String legacyPrefix;
	private boolean cacheEverything;
	private boolean hasPersistentGrowth;

	private Set<PlantGrowthConfig> plantConfigs;

	public RBConfigManager(ACivMod plugin) {
		super(plugin);
	}

	public boolean cacheEverything() {
		return cacheEverything;
	}

	public ManagedDatasource getDatabase() {
		return database;
	}

	public String getLegacyPrefix() {
		return legacyPrefix;
	}

	public Set<PlantGrowthConfig> getPlantGrowthConfigs() {
		return plantConfigs;
	}

	public boolean hasPersistentGrowthConfigs() {
		return hasPersistentGrowth;
	}

	private Map<String, List<Biome>> loadBiomeAliases(ConfigurationSection config) {
		Map<String, List<Biome>> result = new HashMap<>();
		if (config == null) {
			return result;
		}
		for (String key : config.getKeys(false)) {
			if (!config.isList(key)) {
				logger.warning(
						"Invalid non-list entry " + key + " found at " + config.getCurrentPath() + ". It was ignored.");
				continue;
			}
			List<String> biomeStrings = config.getStringList(key);
			List<Biome> biomes = new ArrayList<>();
			for (String biomeString : biomeStrings) {
				try {
					Biome biome = Biome.valueOf(biomeString.toUpperCase().trim());
					biomes.add(biome);
				} catch (IllegalArgumentException e) {
					logger.warning(biomeString + " at " + config.getCurrentPath() + "." + key
							+ " is not a valid biome, it was ignored");
				}
			}
			result.put(key, biomes);
		}
		return result;
	}

	@Override
	protected boolean parseInternal(ConfigurationSection config) {
		hasPersistentGrowth = false;
		database = (ManagedDatasource) config.get("database", null);
		legacyPrefix = config.getString("database_prefix", null);
		Map<String, List<Biome>> biomeAliases = loadBiomeAliases(config.getConfigurationSection("biome_aliases"));
		cacheEverything = config.getBoolean("cache_entire_database", false);
		plantConfigs = parsePlantGrowthConfig(config.getConfigurationSection("plants"), biomeAliases);

		return true;
	}

	private Map<Material, Double> parseMaterialDoubleMap(ConfigurationSection parent, String identifier) {
		Map<Material, Double> result = new EnumMap<>(Material.class);
		ConfigParsing.parseKeyValueMap(parent, identifier, logger, Material::valueOf,  Double::parseDouble,
				result);
		return result;
	}

	private Set<PlantGrowthConfig> parsePlantGrowthConfig(ConfigurationSection config,
			Map<String, List<Biome>> biomeAliases) {
		Set<PlantGrowthConfig> result = new HashSet<>();
		if (config == null) {
			logger.warning("No plant growth configs found");
			return result;
		}
		for (String key : config.getKeys(false)) {
			if (!config.isConfigurationSection(key)) {
				logger.warning("Ignoring invalid entry " + key + " at " + config.getCurrentPath());
				continue;
			}
			ConfigurationSection current = config.getConfigurationSection(key);
			Material material;
			try {
				material = Material.valueOf(key.toUpperCase());
			} catch (IllegalArgumentException e) {
				logger.warning("Could not parse material at " + current.getCurrentPath() + ". Section was ignored");
				continue;
			}
			Map<Material, Double> greenHouseRates = parseMaterialDoubleMap(current, "greenhouse_rates");
			Map<Material, Double> soilBoniPerLevel = parseMaterialDoubleMap(current, "soil_boni");
			Map<Biome, Double> biomeMultiplier = new EnumMap<>(Biome.class);
			if (current.isConfigurationSection("biomes")) {
				ConfigurationSection greenHouseSection = current.getConfigurationSection("biomes");
				for (String biomeKey : greenHouseSection.getKeys(false)) {
					if (!greenHouseSection.isDouble(biomeKey)) {
						logger.warning(
								"Ignoring invalid biome multiplier entry " + key + " at " + current.getCurrentPath());
						continue;
					}
					double value = greenHouseSection.getDouble(biomeKey);

					try {
						Biome biome = Biome.valueOf(biomeKey.toUpperCase());
						biomeMultiplier.put(biome, value);
					} catch (IllegalArgumentException e) {
						if (!biomeAliases.containsKey(biomeKey.toLowerCase())) {
							logger.warning("Failed to parse biome or biome alias " + biomeKey + " at "
									+ current.getCurrentPath());
							continue;
						}
						List<Biome> biomelist = biomeAliases.get(biomeKey.toLowerCase());
						for (Biome bio : biomelist) {
							biomeMultiplier.put(bio, value);
						}
					}
				}
			}
			Long persistTime = null;
			if (current.isString("persistent_growth_period")) {
				persistTime = ConfigParsing.parseTime(current.getString("persistent_growth_period"),
						TimeUnit.MILLISECONDS);
			}
			double baseChance = current.getDouble("base_rate", 1.0);
			BiomeGrowthConfig biomeGrowth;
			if (persistTime != null) {
				biomeGrowth = new PersistentGrowthConfig(persistTime, biomeMultiplier);
				hasPersistentGrowth = true;
			} else {
				biomeGrowth = new ChanceBasedGrowthConfig(baseChance, biomeMultiplier);
			}
			int maximumSoilLayers = current.getInt("soil_max_layers", 0);
			double maximumSoilBonus = current.getDouble("max_soil_bonus", Integer.MAX_VALUE);
			boolean allowBoneMeal = current.getBoolean("allow_bonemeal", false);
			boolean needsLight = current.getBoolean("needs_sun_light", true);
			PlantGrowthConfig growthConfig = new PlantGrowthConfig(key, material, greenHouseRates, soilBoniPerLevel,
					maximumSoilLayers, maximumSoilBonus, allowBoneMeal, biomeGrowth, needsLight);
			result.add(growthConfig);
			logger.info("Successfully parsed growth config for " + material.toString() + " as " + biomeGrowth.toString()
					+ " with green house rates " + greenHouseRates.toString() + ", with soil boni "
					+ soilBoniPerLevel.toString() + ", maximum soil layers: " + maximumSoilLayers
					+ ", maximum soil bonus: " + maximumSoilBonus + ", bonemeal: " + allowBoneMeal + ", needs light: "
					+ needsLight);

		}
		return result;
	}

}
