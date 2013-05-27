package com.untamedears.realisticbiomes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

public class GrowthConfig {
	// a rate of growth between 0 and 1
	// this usually represents a chance,
	// for a crop, it would be the chance to grow per tick
	// for an animal, it would be the chance to spawn after mating
	private double baseRate;
	// this rate overrides all other settings if the plant is under artificial light (adjacent to glowstone)
	private double greenhouseRate;
	private boolean isGreenhouseEnabled;
	private boolean greenhouseIgnoreBiome;
	
	// flag that denotes if this crop's growth is persisted
	private boolean isPersistent;
	private double persistentRate;
	
	// a crop's growth rate can be modulated by the amount of sunlight, not just light in general
	// the crop's growth rate may also get a bonus if it is directly open to the sky, or underneath glowstone (not yet)
	private boolean needsSunlight;
	
	// multiplier that is applied if the crop is not at light level = 15
	private double notFullSunlightMultiplier;
	
	// multiplier that is applied if the crop is not near "fresh water(river biome)"
	private double notIrrigatedMultiplier;
	
	// some crops get a boost from layers of materials beneath the block the plant has been planted on
	private Material soilMaterial;
	private int soilMaxLayers;
	private double soilBonusPerLevel;
	// the z levels below the actual growth event location in which to start looking for the correct soil
	private int soilLayerOffset;
	
	// map from biome to the modulated growth rate per biome
	private Map<Biome, Double> biomeMultipliers;
	
	// conversion used for persistence calculations
	private static final int MS_PER_HOUR = 1000 * 60 * 60;
	// maximum light level on a block
	private static final double MAX_LIGHT_INTENSITY = 15.0;
	
	// ------------------------------------------------------------------------
	
	public static Logger LOG = Logger.getLogger("RealisticBiomes");
	
	// ========================================================================
	// Initialization
	
	// relative locations of visible adjacent blocks
	@SuppressWarnings("serial")
	private static List<Vector> adjacentBlocks = new ArrayList<Vector>(){{
		this.add(new Vector(0,1,0));	// up
		this.add(new Vector(-1,0,0));	// west
		this.add(new Vector(1,0,0));	// east
		this.add(new Vector(0,0,-1));	// north
		this.add(new Vector(0,0,1));	// south
	}};
	private static List<Vector> waterCheckBlocks = new ArrayList<Vector>(){{
		this.add(new Vector(-5,-1,0));	// west
		this.add(new Vector(5,-1,0));	// east
		this.add(new Vector(0,-1,-5));	// north
		this.add(new Vector(0,-1,5));	// south
	}};
	
	public static GrowthConfig get(ConfigurationSection conf, GrowthConfig parent, Map<String, Biome[]>biomeAliases) {
		GrowthConfig growth = new GrowthConfig(parent);
		
		return growth;
	}
	
	// create a new default configuration
	GrowthConfig() {
		baseRate = 1.0;
		
		greenhouseRate = 1.0;
		isGreenhouseEnabled = false;
		greenhouseIgnoreBiome = false;
		
		isPersistent = false;
		
		needsSunlight = false;
		
		notFullSunlightMultiplier = 1.0;
		
		notIrrigatedMultiplier = 1.0;
		
		soilMaterial = null; /* none */
		soilMaxLayers = 0;
		soilBonusPerLevel = 0.0;
		soilLayerOffset = 1;
		
		biomeMultipliers = new HashMap<Biome, Double>();
	}
	
	// make a copy of the given configuration
	GrowthConfig(GrowthConfig parent) {
		baseRate = parent.baseRate;
		
		greenhouseRate = parent.greenhouseRate;
		isGreenhouseEnabled = parent.isGreenhouseEnabled;
		greenhouseIgnoreBiome = parent.greenhouseIgnoreBiome;
		
		needsSunlight = parent.needsSunlight;
		
		notFullSunlightMultiplier = parent.notFullSunlightMultiplier;
		
		notIrrigatedMultiplier = parent.notIrrigatedMultiplier;
		
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
		
		if (config.isSet("greenhouse_rate")) {
			isGreenhouseEnabled = true;
			greenhouseRate = config.getDouble("greenhouse_rate");
		}
		
		if (config.isSet("greenhouse_ignore_biome")) {
			greenhouseIgnoreBiome = config.getBoolean("greenhouse_ignore_biome");
		}
		
		isPersistent = false;
		if (config.isSet("persistent_growth_period")) {
			isPersistent = true;
			persistentRate = config.getDouble("persistent_growth_period");
		}
		
		if (config.isSet("needs_sunlight"))
			needsSunlight = config.getBoolean("needs_sunlight");
		
		if (config.isSet("not_full_sunlight_multiplier"))
			notFullSunlightMultiplier = config.getDouble("not_full_sunlight_multiplier");
		
		if (config.isSet("not_irrigated_multiplier"))
			notIrrigatedMultiplier = config.getDouble("not_irrigated_multiplier");
		
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
		if (isPersistent) {
			rate = 1.0 / (persistentRate * MS_PER_HOUR);
		}
		
		double environmentMultiplier = 1.0;
		
		// biome multiplier
		Double biomeMultiplier = biomeMultipliers.get(block.getBiome());
		if (biomeMultiplier != null) {
			environmentMultiplier *= biomeMultiplier.floatValue();
		} else {
			return 0.0; // if the biome cannot be found, assume zero
		}
		
		// if the greenhouse effect does not ignore biome, fold the biome rate into the main rate directly
		if (!greenhouseIgnoreBiome) {
			rate *= environmentMultiplier;
			environmentMultiplier = 1.0;
		}
		
		// modulate the rate by the amount of sunlight recieved by this plant
		int sunlightIntensity = block.getLightFromSky();
		if (needsSunlight) {
			environmentMultiplier *= Math.pow((sunlightIntensity / MAX_LIGHT_INTENSITY), 3.0);
		}
		// apply multiplier if the sunlight is not at maximum
		if (sunlightIntensity < MAX_LIGHT_INTENSITY) {
			environmentMultiplier *= notFullSunlightMultiplier;
		}
		
		// if the crop block if fully lit, and the greenhouse rate would be an improvement
		// over the current environment multiplier, then use the green house rate as the
		// environment multiplier.
		if(isGreenhouseEnabled && ( environmentMultiplier < greenhouseRate ) && ( block.getLightFromBlocks() == (MAX_LIGHT_INTENSITY - 1) ) ) {
			// make sure it's a glowstone/lamp
			for( Vector vec : adjacentBlocks ) {
				Material mat = block.getLocation().add(vec).getBlock().getType();
				if( mat == Material.GLOWSTONE || mat == Material.REDSTONE_LAMP_ON ) {
					environmentMultiplier = greenhouseRate;
					break;
				}
			}
		}
		
		rate *= environmentMultiplier;
		
		// if the plant will be effected by irrigation, check if the block is at the correct level and
		// check if nearby blocks are water blocks in river biomes
		if (notIrrigatedMultiplier != 1.0) {
			// determine if the block is near a water block in a river biome
			boolean irrigated = false;
			for( Vector vec : waterCheckBlocks ) {
				Block waterBlock = block.getLocation().add(vec).getBlock();
				Material mat = waterBlock.getType();
				Biome biome = waterBlock.getBiome();
				if((biome == Biome.RIVER || biome == Biome.FROZEN_RIVER) && (mat == Material.STATIONARY_WATER || mat == Material.WATER)) {
					irrigated = true;
					break;
				}
			}
			
			if (!irrigated)
				rate *= notIrrigatedMultiplier;
		}
		
		// check the depth of the required 'soil' and add a bonus
		float soilBonus = 0.0f;
		Block newBlock = block.getRelative(0,-soilLayerOffset,0);	
		int soilCount = 0;
		while (soilCount < soilMaxLayers) {
			if (newBlock == null || !newBlock.getType().equals(soilMaterial)) {
				break;
			}
				
			soilBonus += soilBonusPerLevel;
			
			newBlock = newBlock.getRelative(0, -1, 0);
			soilCount++;
		}
		rate *= (1.0 + soilBonus);
		
		return rate;
	}
}
