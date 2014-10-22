package com.untamedears.realisticbiomes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

public class BaseConfig {
	// a rate of growth between 0 and 1
	// this represents a chance,
	// for a fishing reward, it's an additional chance of the item dropping once Minecraft has already chosen to drop it
	protected double baseRate;

	// map from biome to the modulated growth rate per biome
	protected Map<Biome, Double> biomeMultipliers;

	// ------------------------------------------------------------------------

	public static Logger LOG = Logger.getLogger("RealisticBiomes");

	// ========================================================================
	// Initialization

	public static BaseConfig get(ConfigurationSection conf, BaseConfig parent, Map<String, Biome[]>biomeAliases) {
		BaseConfig config = new BaseConfig(parent);
		return config;
	}

	// create a new default configuration
	public BaseConfig() {
		baseRate = 1.0;
		biomeMultipliers = new HashMap<Biome, Double>();
	}

	// make a copy of the given configuration
	public BaseConfig(BaseConfig parent) {
		copy(parent);
		baseRate = parent.baseRate;
		biomeMultipliers = new HashMap<Biome, Double>(parent.biomeMultipliers);
	}

	// make a copy of the given configuration and modify it by loading in a YML config section
	public BaseConfig(BaseConfig parent, ConfigurationSection config, HashMap<String, List<Biome>> biomeAliases) {
		copy(parent);
		if (config.isSet("base_rate"))
			baseRate = config.getDouble("base_rate");
		if (config.isSet("biomes"))
			loadBiomes(config.getConfigurationSection("biomes"), biomeAliases);
	}

	public void copy(BaseConfig other) {
		baseRate = other.baseRate;
		biomeMultipliers = new HashMap<Biome, Double>(other.biomeMultipliers);
	}

	public void loadBiomes(ConfigurationSection config, HashMap<String, List<Biome>> biomeAliases) {
		for (String biomeName : config.getKeys(false)) {
			if (biomeAliases.containsKey(biomeName)) {
				// if there is a biome alias with the name, register all biomes of that alias with the
				// given multiplier
				double multiplier = config.getDouble(biomeName);
				for (Biome biome : biomeAliases.get(biomeName)) {
					biomeMultipliers.put(biome, multiplier);
				}
			}
			else {
				// else just register the given biome with the multiplier
				try {
					Biome biome = Biome.valueOf(biomeName);
					biomeMultipliers.put(biome, config.getDouble(biomeName));
				}
				catch(IllegalArgumentException e) {
					LOG.warning("loading configs: in \""+ config.getParent().getName() +"\" biomes: \"" + biomeName +"\" is not a valid biome name.");
				}
			}
		}
	}

	/* ================================================================================ */
	// Public Methods

	// given a block (a location), find the rate using these rules
	public double getRate(Block block) {
		// rate = baseRate * biome
		double rate = baseRate;
		// biome multiplier
		Double biomeMultiplier = biomeMultipliers.get(block.getBiome());
		if (biomeMultiplier != null) {
			rate *= biomeMultiplier.floatValue();
		} else {
			rate = 0.0D; // if the biome cannot be found, assume zero
		}
		return rate;
	}
}
