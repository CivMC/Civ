package com.untamedears.realisticbiomes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

public class GrowthConfig {
	// a rate of growth between 0 and 1
	// this usually represents a chance,
	// for a crop, it would be the chance to grow per tick
	// for an animal, it would be the chance to spawn after mating
	private double baseRate;
	
	// flag that denotes if this crop's growth is persisted
	private boolean isPersistent;
	private double persistentRate;
	
	// a crop's growth rate can be modulated by the amount of sunlight, not just light in general
	// the crop's growth rate may also get a bonus if it is directly open to the sky, or underneath glowstone (not yet)
	private boolean needsSunlight;
	private double openSkyBonus;
	
	// some crops get a boost from layers of materials beneath the block the plant has been planted on
	private Material soilMaterial;
	private int soilMaxLayers;
	private double soilBonusPerLevel;
	// the z levels below the actual growth event location in which to start looking for the correct soil
	private int soilLayerOffset;
	
	// map from biome to the modulated growth rate per biome
	private Map<Biome, Double> biomeMultipliers;
	
	// ------------------------------------------------------------------------
	
	public static Logger LOG = Logger.getLogger("RealisticBiomes");
	
	// ========================================================================
	// Initialization
	
	public static GrowthConfig get(ConfigurationSection conf, GrowthConfig parent, Map<String, Biome[]>biomeAliases) {
		GrowthConfig growth = new GrowthConfig(parent);
		
		return growth;
	}
	
	// create a new default configuration
	GrowthConfig() {
		baseRate = 1.0;
		isPersistent = false;
		
		needsSunlight = false;
		openSkyBonus = 0.0;
		
		soilMaterial = null; /* none */
		soilMaxLayers = 0;
		soilBonusPerLevel = 0.0;
		soilLayerOffset = 1;
		
		biomeMultipliers = new HashMap<Biome, Double>();
	}
	
	// make a copy of the given configuration
	GrowthConfig(GrowthConfig parent) {
		baseRate = parent.baseRate;
		
		needsSunlight = parent.needsSunlight;
		openSkyBonus = parent.openSkyBonus;
		
		soilMaterial = parent.soilMaterial;
		soilMaxLayers = parent.soilMaxLayers;
		soilBonusPerLevel = parent.soilBonusPerLevel;
		soilLayerOffset = parent.soilLayerOffset;
		
		biomeMultipliers = new HashMap<Biome, Double>(parent.biomeMultipliers);
	}
	
	// make a copy of the given configuration and modify it by loading in a YML config section
	GrowthConfig(GrowthConfig parent, ConfigurationSection config, HashMap<String, List<Biome>> biomeAliases) {
		this(parent);
		
		if (config.isSet("base_rate"))
			baseRate = config.getDouble("base_rate");
		
		isPersistent = false;
		if (config.isSet("persistent_growth_period")) {
			isPersistent = true;
			persistentRate = config.getDouble("persistent_growth_period");
		}
		
		if (config.isSet("needs_sunlight"))
			needsSunlight = config.getBoolean("needs_sunlight");
		
		if (config.isSet("open_sky_bonus"))
			openSkyBonus = config.getDouble("open_sky_bonus");
		
		if (config.isSet("soil_material")) {
			String materialName = config.getString("soil_material");
			Material material = Material.getMaterial(materialName);
			if (material == null)
				LOG.warning("loading configs: \""+ config.getName() +"\" soil_material: \"" + materialName +"\" is not a valid material name.");
			else
				soilMaterial = material;
		}
		
		if (config.isSet("soil_max_layers"))
			soilMaxLayers = config.getInt("soil_max_layers");
		
		if (config.isSet("soil_bonus_per_layer"))
			soilBonusPerLevel = config.getDouble("soil_bonus_per_layer");
		
		if (config.isSet("soil_layer_offset"))
			soilLayerOffset = config.getInt("soil_layer_offset");
		
		if (config.isSet("biomes"))
			loadBiomes(config.getConfigurationSection("biomes"), biomeAliases);
	}
	
	private void loadBiomes(ConfigurationSection config, HashMap<String, List<Biome>> biomeAliases) {
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
	
	public boolean isPersistent() {
		return isPersistent;
	}
	
	// given a block (a location), find the growth rate using these rules
	public double getRate(Block block) {
		// rate = baseRate * sunlightLevel * biome * (1.0 + soilBonus)
		double rate = baseRate;
		// if persistent, the growth rate is measured in growth/millisecond
		if (isPersistent)
			rate = 1.0 / (persistentRate * (1000.0*60.0*60.0/*milliseconds per hour*/));
		
		// modulate the rate by the amount of sunlight recieved by this plant
		if (needsSunlight) {
			rate *= block.getLightFromSky() / 15.0;
		}
		
		// biome multiplier
		Double biomeMultiplier = biomeMultipliers.get(block.getBiome());
		if (biomeMultiplier != null)
			rate *= biomeMultiplier.floatValue();
		else
			return 0; // if the biome cannot be found, assume zero
		
		// check if the block is directly below the sky for a bonus
		if (openSkyBonus != 0.0) {
			Block newBlock = block.getRelative(0, 1, 0);

			int y = 0;
			
			Material m = Material.AIR;
			while ((m == Material.AIR || m == Material.LEAVES || m == Material.VINE)) {
				m = newBlock.getType();
				y = newBlock.getY();
				if (y == 255) {
					rate *= openSkyBonus;
					break;
				}
				newBlock = newBlock.getRelative(0, 1, 0);
			}
		}
		
		// check the depth of the required soil and add a bonus
		float soilBonus = 0.0f;
		Block newBlock = block.getRelative(0,-soilLayerOffset,0);	
		int soilCount = 0;
		while (soilCount < soilMaxLayers) {
			if (newBlock == null || !newBlock.getType().equals(soilMaterial))
				break;
				
			soilBonus += soilBonusPerLevel;
			
			newBlock = newBlock.getRelative(0, -1, 0);
			soilCount++;
		}
		rate *= (1.0 + soilBonus);
		
		return rate;
	}
}
