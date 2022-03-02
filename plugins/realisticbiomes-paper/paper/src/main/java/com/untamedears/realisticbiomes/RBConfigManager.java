package com.untamedears.realisticbiomes;

import static vg.civcraft.mc.civmodcore.config.ConfigHelper.parseMaterialList;


import com.untamedears.realisticbiomes.growth.AgeableGrower;
import com.untamedears.realisticbiomes.growth.BambooGrower;
import com.untamedears.realisticbiomes.growth.ColumnPlantGrower;
import com.untamedears.realisticbiomes.growth.FruitGrower;
import com.untamedears.realisticbiomes.growth.FungusGrower;
import com.untamedears.realisticbiomes.growth.HorizontalBlockSpreadGrower;
import com.untamedears.realisticbiomes.growth.IArtificialGrower;
import com.untamedears.realisticbiomes.growth.KelpGrower;
import com.untamedears.realisticbiomes.growth.SchematicGrower;
import com.untamedears.realisticbiomes.growth.SeaPickleGrower;
import com.untamedears.realisticbiomes.growth.StemGrower;
import com.untamedears.realisticbiomes.growth.TipGrower;
import com.untamedears.realisticbiomes.growth.TreeGrower;
import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;
import com.untamedears.realisticbiomes.growthconfig.inner.BiomeGrowthConfig;
import com.untamedears.realisticbiomes.growthconfig.inner.ChanceBasedGrowthConfig;
import com.untamedears.realisticbiomes.growthconfig.inner.PersistentGrowthConfig;
import com.untamedears.realisticbiomes.model.RBSchematic;
import com.untamedears.realisticbiomes.model.ltree.BlockTransformation;
import com.untamedears.realisticbiomes.model.ltree.LStepConfig;
import com.untamedears.realisticbiomes.model.ltree.LTree;
import com.untamedears.realisticbiomes.model.ltree.SpreadRule;
import com.untamedears.realisticbiomes.utils.SchematicUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.config.ConfigHelper;
import vg.civcraft.mc.civmodcore.config.ConfigParser;
import vg.civcraft.mc.civmodcore.dao.DatabaseCredentials;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;

public class RBConfigManager extends ConfigParser {

	private ManagedDatasource database;

	private String legacyPrefix;
	private boolean hasPersistentGrowth;

	private Set<PlantGrowthConfig> plantConfigs;
	private Map<String, RBSchematic> schematics;
	private List<LTree> lTrees;

	private List<Material> bonemealPreventedBlocks;

	public RBConfigManager(ACivMod plugin) {
		super(plugin);
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

	private void loadSchematics() {
		this.schematics = new HashMap<>();
		File folder = new File(plugin.getDataFolder(), "schematics");
		for (RBSchematic schem : SchematicUtils.loadAll(folder, logger)) {
			this.schematics.put(schem.getName().toLowerCase(), schem);
		}
	}

	@Override
	protected boolean parseInternal(ConfigurationSection config) {
		hasPersistentGrowth = false;
		database = ManagedDatasource.construct((ACivMod) plugin, (DatabaseCredentials) config.get("database"));
		legacyPrefix = config.getString("database_prefix", null);
		Map<String, List<Biome>> biomeAliases = loadBiomeAliases(config.getConfigurationSection("biome_aliases"));
		loadSchematics();
		plantConfigs = parsePlantGrowthConfig(config.getConfigurationSection("plants"), biomeAliases);
		remapStemFruitConfigs();
		List<LStepConfig> rawConfigs = parseRawLStepConfigs(config.getConfigurationSection("l_steps"));
		lTrees = parseLTrees(config.getConfigurationSection("l_trees"), rawConfigs);
		bonemealPreventedBlocks = parseMaterialList(config, "no_bonemeal_blocks");
		return true;
	}

	private void remapStemFruitConfigs() {
		for (PlantGrowthConfig plantConfig : plantConfigs) {
			if (!(plantConfig.getGrower() instanceof StemGrower)) {
				continue;
			}
			StemGrower stemGrower = (StemGrower) plantConfig.getGrower();
			for (PlantGrowthConfig otherConfig : plantConfigs) { // dont care about O(n) here
				if (otherConfig.getName().equalsIgnoreCase(stemGrower.getFruitConfigName())) {
					stemGrower.setFruitConfig(otherConfig);
					break;
				}
			}
			if (stemGrower.getFruitConfig() == null) {
				logger.severe("Stem config " + plantConfig.getName() + " specified fruit type "
						+ stemGrower.getFruitConfigName() + ", but no such config was found");
			}

		}
	}

	private Map<Material, Double> parseMaterialDoubleMap(ConfigurationSection parent, String identifier) {
		Map<Material, Double> result = new EnumMap<>(Material.class);
		ConfigHelper.parseKeyValueMap(parent, identifier, logger, Material::valueOf, Double::parseDouble, result);
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
			if (!current.isItemStack("item")) {
				logger.warning("Growth config " + key + " does not have an item specified, it was ignored");
				continue;
			}
			ItemStack item = current.getItemStack("item", null);
			List<Material> vanillaMats = parseMaterialList(current, "vanilla_materials");
			if (vanillaMats == null) {
				vanillaMats = Collections.emptyList();
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
				persistTime = ConfigHelper.parseTime(current.getString("persistent_growth_period"),
						TimeUnit.MILLISECONDS);
			}
			String name = current.getString("name", key);
			double baseChance = current.getDouble("base_rate", 1.0);
			BiomeGrowthConfig biomeGrowth;
			if (persistTime != null) {
				biomeGrowth = new PersistentGrowthConfig(persistTime, biomeMultiplier);
				hasPersistentGrowth = true;
			} else {
				biomeGrowth = new ChanceBasedGrowthConfig(baseChance, biomeMultiplier);
			}
			short id = (short) current.getInt("id", 0);
			if (id == 0) {
				logger.warning(
						"Need to specify a non 0 id for each plant type, missing at " + current.getCurrentPath());
				continue;
			}
			int maximumSoilLayers = current.getInt("soil_max_layers", 0);
			double maximumSoilBonus = current.getDouble("max_soil_bonus", Integer.MAX_VALUE);
			boolean allowBoneMeal = current.getBoolean("allow_bonemeal", false);
			boolean needsLight = current.getBoolean("needs_sun_light", true);
			boolean canBePlantedDirectly = current.getBoolean("can_be_planted", true);
			boolean needsToBeWaterLogged = current.getBoolean("waterlog_required", false);
			IArtificialGrower grower = parseGrower(current.getConfigurationSection("grower"), item);
			if (grower == null) {
				logger.warning("Failed to parse a grower at " + current.getCurrentPath() + ", skipped it");
				continue;
			}
			PlantGrowthConfig growthConfig = new PlantGrowthConfig(name, id, item, greenHouseRates, soilBoniPerLevel,
					maximumSoilLayers, maximumSoilBonus, allowBoneMeal, biomeGrowth, needsLight, grower, vanillaMats,
					canBePlantedDirectly, needsToBeWaterLogged);
			result.add(growthConfig);
		}
		return result;
	}

	private IArtificialGrower parseGrower(ConfigurationSection section, ItemStack item) {
		if (section == null) {
			return null;
		}
		if (!section.isString("type")) {
			logger.warning("No grower type specified at " + section.getCurrentPath());
			return null;
		}
		Material material = MaterialUtils.getMaterial(section.getString("material"));
		if (material == null) {
			if (item == null) {
				logger.warning("Neither an item nor a material specified for grower at " + section.getCurrentPath());
				return null;
			}
			material = item.getType();
		}
		switch (section.getString("type").toLowerCase()) {
			case "bamboo":
				int maxHeight = section.getInt("max_height", 12);
				return new BambooGrower(maxHeight);
			case "column":
				int maxHeight2 = section.getInt("max_height", 3);
				boolean instaBreakTouching = section.getBoolean("insta_break_toching", false);
				BlockFace direction = BlockFace.valueOf(section.getString("direction", "UP"));
				return new ColumnPlantGrower(maxHeight2, material, direction, instaBreakTouching);
			case "fruit":
				Material stemMat = MaterialUtils.getMaterial(section.getString("stem_type"));
				if (stemMat == null) {
					logger.warning("No stem material specified at " + section.getCurrentPath());
					return null;
				}
				Material attachedStemMat = MaterialUtils.getMaterial(section.getString("attached_stem_type"));
				if (attachedStemMat == null) {
					logger.warning("No attached stem material specified at " + section.getCurrentPath());
					return null;
				}
				return new FruitGrower(material, attachedStemMat, stemMat);
			case "fungus":
				return new FungusGrower(material);
			case "tip":
				Material stem = MaterialUtils.getMaterial(section.getString("stem_material"));
				BlockFace growthDirection = BlockFace.valueOf(section.getString("growth_direction"));
				int maxHeight3 = section.getInt("max_height", 25);
				return new TipGrower(material, stem, growthDirection, maxHeight3);
			case "kelp":
				int maxHeight4 = section.getInt("max_height", 25);
				return new KelpGrower(maxHeight4);
			case "ageable":
				int maxStage = section.getInt("max_stage", 7);
				int increment = section.getInt("increment", 1);
				return new AgeableGrower(material, maxStage, increment);
			case "stem":
				String fruitConfig = section.getString("fruit_config", null);
				return new StemGrower(material, fruitConfig);
			case "tree":
				return new TreeGrower(material);
			case "horizontalspread":
				int maxAmount = section.getInt("max_amount");
				int horRange = section.getInt("max_range");
				List<Material> replaceableBlocks = parseMaterialList(section, "replaceable_blocks");
				List<Material> validSoil = parseMaterialList(section, "valid_soil");
				return new HorizontalBlockSpreadGrower(material, maxAmount, horRange, replaceableBlocks, validSoil);
			case "seapickle":
				return new SeaPickleGrower();
			case "schematic":
				String name = section.getString("schematic", "default");
				RBSchematic schem = schematics.get(name.toLowerCase());
				if (schem == null) {
					logger.warning(
							"Schematic " + name + " specified at " + section.getCurrentPath() + " was not found");
					return null;
				}
				Location offSet = section.getLocation("offset", new Location(null, 0, 0, 0));
				return new SchematicGrower(schem, offSet);
			default:
				logger.warning(section.getString("type") + " is not a valid grower type");
				return null;
		}
	}

	public List<LTree> parseLTrees(ConfigurationSection config, List<LStepConfig> stepConfig) {
		List<LTree> result = new ArrayList<>();
		if (config == null) {
			return result;
		}
		Map<String, LStepConfig> configMap = stepConfig.stream().collect(Collectors.toMap(LStepConfig::getID, l -> l));
		for (String key : config.getKeys(false)) {
			if (!config.isConfigurationSection(key)) {
				logger.warning("Found invalid entry " + key + " at " + config.getCurrentPath());
				continue;
			}
			ConfigurationSection current = config.getConfigurationSection(key);
			String start = current.getString("start_symbol");
			Vector defaultVector = current.getVector("start_direction");
			Map<String, List<SpreadRule>> spreadRules = new HashMap<>();
			ConfigurationSection spreadSection = current.getConfigurationSection("rules");
			if (spreadSection != null) {
				for (String spreadKey : spreadSection.getKeys(false)) {
					List<SpreadRule> localSpreadRules = new ArrayList<>();
					ConfigurationSection currentKeySection = spreadSection.getConfigurationSection(spreadKey);
					for (String subKey : currentKeySection.getKeys(false)) {
						ConfigurationSection subSection = currentKeySection.getConfigurationSection(subKey);
						double chance = subSection.getDouble("chance", 1.0);
						List<String> targets = new ArrayList<>();
						List<Vector> targetDirections = new ArrayList<>();
						ConfigurationSection resultSection = subSection.getConfigurationSection("results");
						if (resultSection != null) {
							for (String resultKey : resultSection.getKeys(false)) {
								ConfigurationSection currentResultSection = resultSection
										.getConfigurationSection(resultKey);
								String name = currentResultSection.getString("name");
								Vector direction = currentResultSection.getVector("direction");
								if (name == null || direction == null) {
									logger.warning("Incomplete entry at " + currentResultSection.getCurrentPath()
											+ " was ignored");
									continue;
								}
								targets.add(name);
								targetDirections.add(direction);
							}
						}
						localSpreadRules.add(new SpreadRule(chance, targets, targetDirections));
					}
					spreadRules.put(spreadKey, localSpreadRules);
				}
			}
			result.add(new LTree(key, start, defaultVector, spreadRules, i -> {
				LStepConfig lConfig = configMap.get(i);
				if (lConfig != null) {
					lConfig = lConfig.clone();
				} else {
					logger.warning("No lstep config with name " + i + " was specified, but it was set as target");
				}
				return lConfig;
			}));
		}
		return result;
	}

	public List<LTree> getLTrees() {
		return lTrees;
	}

	public List<Material> getBonemealPreventedBlocks() {
		return bonemealPreventedBlocks;
	}

	public List<LStepConfig> parseRawLStepConfigs(ConfigurationSection config) {
		List<LStepConfig> result = new ArrayList<>();
		if (config == null) {
			return result;
		}
		for (String key : config.getKeys(false)) {
			if (!config.isConfigurationSection(key)) {
				logger.warning("Found invalid entry " + key + " at " + config.getCurrentPath());
				continue;
			}
			ConfigurationSection current = config.getConfigurationSection(key);
			double directionWeight = current.getDouble("direction_weight", 1.0);
			List<BlockTransformation> transforms = parseBlockTransformations(
					current.getConfigurationSection("transformations"));
			boolean normalizeBeforeDirectionTransformation = current
					.getBoolean("normalize_direction_before_transformation", true);
			boolean transformInWorldCoordinates = current.getBoolean("transform_in_world_coords", false);
			result.add(new LStepConfig(key, directionWeight, transforms, normalizeBeforeDirectionTransformation,
					transformInWorldCoordinates));
		}
		return result;
	}

	private Vector parseVector(ConfigurationSection config, String key) {
		String value = config.getString(key);
		String[] split = value.split(",");
		if (split.length != 3) {
			return null;
		}
		try {
			double x = Double.parseDouble(split[0].trim());
			double y = Double.parseDouble(split[1].trim());
			double z = Double.parseDouble(split[2].trim());
			return new Vector(x, y, z);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private List<BlockTransformation> parseBlockTransformations(ConfigurationSection config) {
		List<BlockTransformation> result = new ArrayList<>();
		if (config == null) {
			return result;
		}
		for (String key : config.getKeys(false)) {
			if (!config.isConfigurationSection(key)) {
				logger.warning("Found invalid entry " + key + " at " + config.getCurrentPath());
				continue;
			}
			ConfigurationSection current = config.getConfigurationSection(key);
			Material mat;
			try {
				mat = Material.valueOf(current.getString("material", ""));
			} catch (IllegalArgumentException e) {
				logger.warning("No material specified at " + current.getCurrentPath());
				continue;
			}
			Map<String, String> blockData = new HashMap<>();
			ConfigurationSection dataSection = current.getConfigurationSection("data");
			if (dataSection != null) {
				for (String dataKey : dataSection.getKeys(false)) {
					String value = dataSection.getString(dataKey);
					blockData.put(dataKey, value);
				}
			}

			result.add(new BlockTransformation(mat, blockData));
		}
		return result;
	}

}
