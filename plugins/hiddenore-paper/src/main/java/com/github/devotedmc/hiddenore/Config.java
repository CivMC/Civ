package com.github.devotedmc.hiddenore;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import com.github.devotedmc.hiddenore.listeners.ConfigDeferralListener;

/**
 * Someday it might be nice to refactor this to be a proper object.
 * 
 * For now, this holds the configs for HiddenOre, in an instance-backed semi-private collective.
 * 
 * @author soexpso, programmerdan
 *
 */
public final class Config {

	public static Config instance;
	public static boolean isDebug;

	public String defaultPrefix;
	public boolean alertUser;
	public boolean listDrops;
	public boolean ignoreSilktouch;
	public Map<String, DropConfig> dropConfigs;
	public Map<UUID, Map<NamespacedKey, List<BlockConfig>>> blockConfigs;
	public Map<String, Map<NamespacedKey, List<BlockConfig>>> preloadBlockConfigs;
	public Map<String, NameConfig> prettyNames;
	public Map<String, PlayerStateConfig> stateMasterList;

	private static FileConfiguration file;

	private static String trackFileName;
	private static File trackFile;
	public static long trackSave;
	
	private static boolean useMapSave;
	private static String mapFileName;
	private static File mapFile;
	public static long mapSave;
	
	public int transformAttemptMultiplier = 3;
	
	public static boolean caveOres = false;

	private Config() {
		dropConfigs = new HashMap<String, DropConfig>();
		blockConfigs = new HashMap<UUID, Map<NamespacedKey, List<BlockConfig>>>();
		preloadBlockConfigs = new HashMap<String, Map<NamespacedKey, List<BlockConfig>>>();
		prettyNames = new HashMap<String, NameConfig>();
		stateMasterList = new HashMap<String, PlayerStateConfig>();
		trackFileName = "tracking.dat";
		trackSave = 90000l;
		useMapSave = true;
		mapFileName = "map.dat";
		mapSave = 90000l;
		alertUser = false;
		listDrops = false;
		isDebug = false;
		ignoreSilktouch = false;
		defaultPrefix = "You found hidden ore!";
		transformAttemptMultiplier = 3;
	}

	public static void loadConfig() {
		try {
			file = HiddenOre.getPlugin().getConfig();
			doLoad();
		} catch (Exception e) {
			HiddenOre.getPlugin().getLogger().log(Level.WARNING, "An error occured while loading config!", e);
		}
	}

	public static void doLoad() {
		Config i = new Config();

		isDebug = file.getBoolean("debug", isDebug);
		caveOres = file.getBoolean("caveOres", caveOres);

		trackFileName = file.getString("track_file", trackFileName);
		trackFile = new File(HiddenOre.getPlugin().getDataFolder(), trackFileName);
		trackSave = file.getLong("track_save_ticks", trackSave);
		
		useMapSave = file.getBoolean("map_save_active", useMapSave);
		mapFileName = file.getString("map_file", mapFileName);
		mapFile = new File(HiddenOre.getPlugin().getDataFolder(), mapFileName);
		mapSave = file.getLong("map_save_ticks", mapSave);

		i.ignoreSilktouch = file.getBoolean("ignore_silktouch", i.ignoreSilktouch);

		i.alertUser = file.getBoolean("alert_user", i.alertUser);
		i.listDrops = file.getBoolean("list_drops", i.listDrops);
		i.defaultPrefix = file.getString("prefix", i.defaultPrefix);
		i.transformAttemptMultiplier = file.getInt("transform_attempt_multiplier", i.transformAttemptMultiplier);

		ConfigurationSection prettyNames = file.getConfigurationSection("pretty_names");
		if (prettyNames != null) {
			for (String key : prettyNames.getKeys(false)) {
				NameConfig nc = null;
				/*
				 * As of 1.13, subtypes are removed.
				 */
				if (prettyNames.isConfigurationSection(key)) {
					ConfigurationSection pName = prettyNames.getConfigurationSection(key);
					String name = pName.getString("name", key);
					nc = new NameConfig(name);
					// for 1.13, we're ignoring subtypes. We've kept this stub to encourage cross
					// compat, but they are ignored if designated.
					// No attempt is made to bridge compatibility, even where possible.
				} else if (prettyNames.isString(key)) {
					String name = prettyNames.getString(key, key);
					nc = new NameConfig(name);
				}

				if (nc != null) {
					i.prettyNames.put(key, nc);
				}
			}
		} else {
			HiddenOre.getPlugin().getLogger().info("No Pretty Names specified.");
		}
		
		ConfigurationSection tools = file.getConfigurationSection("tools");
		if (tools != null) {
			for (String key : tools.getKeys(false)) {
				if (tools.isConfigurationSection(key)) {
					ToolConfig.initTool(tools.getConfigurationSection(key));
					HiddenOre.getPlugin().getLogger().info("Tool " + key + " initialized");
				}
			}
		} else {
			HiddenOre.getPlugin().getLogger().info("No tool configurations specified. This might cause issues.");
		}
		
		ConfigurationSection states = file.getConfigurationSection("states");
		if (states != null) {
			for (String state : states.getKeys(false)) {
				if (states.isConfigurationSection(state)) {
					ConfigurationSection stateConfig = states.getConfigurationSection(state);
					PlayerStateConfig pstateConfig = new PlayerStateConfig();
					if (stateConfig.contains("haste")) {
						pstateConfig.hasteRates = stateConfig.getDoubleList("haste");
					}
					if (stateConfig.contains("fatigue" )) {
						pstateConfig.fatigueRates = stateConfig.getDoubleList("fatigue");
					}
					if (stateConfig.contains("nausea")) { 
						pstateConfig.nauseaRates = stateConfig.getDoubleList("nausea");
					}
					if (stateConfig.contains("luck")) {
						pstateConfig.luckRates = stateConfig.getDoubleList("luck");
					}
					if (stateConfig.contains("blindness")) {
						pstateConfig.blindnessRates = stateConfig.getDoubleList("blindness");
					}
					if (stateConfig.contains("badluck")) {
						pstateConfig.badluckRates = stateConfig.getDoubleList("badluck");
					}
					i.stateMasterList.put(state, pstateConfig);
					HiddenOre.getPlugin().getLogger().info("State " + state + " initialized");
				}
			}
		}
		
		ConfigurationSection drops = file.getConfigurationSection("drops");
		if (drops != null) {
			for (String drop : drops.getKeys(false)) {
				if (drops.isConfigurationSection(drop)) {
					i.dropConfigs.put(drop, grabDropConfig(drops, drop));
				}
			}
		}

		ConfigurationSection blocks = file.getConfigurationSection("blocks");
		if (blocks != null) { // default or legacy
			Map<NamespacedKey, List<BlockConfig>> worldBlockConfigs =  new HashMap<NamespacedKey, List<BlockConfig>>();
			grabBlocks("default", worldBlockConfigs, blocks, i);
			i.blockConfigs.put(null, worldBlockConfigs);
		}
		
		ConfigurationSection worlds = file.getConfigurationSection("worlds");
		if (worlds != null) { // has per-world blocks!
			for (String world : worlds.getKeys(false)) {
				UUID worlduid = null;
				try {
					worlduid = UUID.fromString(world);
					if (HiddenOre.getPlugin().getServer().getWorld(worlduid) == null) {
						System.err.println("Although it seems to be a UUID, " + world + " no match found yet.");
						worlduid = null;
					}
				} catch (Exception e) {
					System.out.println("World not defined by UUID");
					worlduid = null;
				}
				
				if (worlduid == null) {
					try {
						worlduid = HiddenOre.getPlugin().getServer().getWorld(world).getUID();
					} catch (Exception f) {
						System.out.println("World not defined by Name; unable to match " + world + " with loaded world.");
					}
				}
				
				Map<NamespacedKey, List<BlockConfig>> worldBlockConfigs = null;
				
				if (worlduid == null) {
					System.err.println("Unable to match world " + world + " with loaded world, registering for post-load.");
					worldBlockConfigs = i.preloadBlockConfigs.get(world);
				} else {
					worldBlockConfigs = i.blockConfigs.get(worlduid);
				}

				if (worldBlockConfigs == null) {
					worldBlockConfigs = new HashMap<NamespacedKey, List<BlockConfig>>();
				}

				ConfigurationSection worldConfig = worlds.getConfigurationSection(world);
				if (worldConfig != null) {
					ConfigurationSection worldBlocks = worldConfig.getConfigurationSection("blocks");
					if (worldBlocks != null) {
						grabBlocks(world, worldBlockConfigs, worldBlocks, i);
					}
				}

				if (worlduid == null) {
					i.preloadBlockConfigs.put(world, worldBlockConfigs);
				} else {
					i.blockConfigs.put(worlduid, worldBlockConfigs);
				}
			}
		}
		
		if (i.preloadBlockConfigs.size() > 0) {
			// some world configs won't immediately resolve!
			// register a listener for world init / loading to check if we can resolve them!
			
			HiddenOre.getPlugin().getServer().getPluginManager()
					.registerEvents(new ConfigDeferralListener(), HiddenOre.getPlugin());
			
		}
		
		instance = i;
	}

	private static void grabBlocks(String world, Map<NamespacedKey, List<BlockConfig>> worldBlockConfigs, ConfigurationSection blocks, Config i) {
		if (blocks != null) {
			for (String sourceBlock : blocks.getKeys(false)) {
				HiddenOre.getPlugin().getLogger().info("Loading config for " + sourceBlock + " for world " + world);
				ConfigurationSection block = blocks.getConfigurationSection(sourceBlock);

				String cBlockName = block.getString("material");
				List<NamespacedKey> cBlockKeys = new ArrayList<NamespacedKey>();
				if (cBlockName == null) {
					ConfigurationSection cBlockNames = block.getConfigurationSection("materials");
					if (cBlockNames == null) {
						HiddenOre.getPlugin().getLogger().warning("Failed to find material or materials for " + sourceBlock);
						continue;
					} else {
						for (String cBlockN : cBlockNames.getKeys(false)) {
							ConfigurationSection cBlockS = cBlockNames.getConfigurationSection(cBlockN);
							String cBlockName2 = cBlockS.getString("material");
							Material cBlockMat = Material.getMaterial(cBlockName2);
							if (cBlockMat == null) {
								HiddenOre.getPlugin().getLogger().warning("Failed to find material for " + cBlockName2);
								continue;
							} else {
								cBlockKeys.add(cBlockMat.getKey());
							}
						}
					}
				} else {
					try {
						Material cBlockMat = Material.getMaterial(cBlockName);
						if (cBlockMat == null) {
							HiddenOre.getPlugin().getLogger().warning("Failed to find material for " + cBlockName);
							continue;
						} else {
							cBlockKeys.add(cBlockMat.getKey());
						}
					} catch (Exception e) {
						HiddenOre.getPlugin().getLogger().warning("Failed to find material for " + cBlockName);
						continue;
					}
				}
				
				if (cBlockKeys.isEmpty()) {
					HiddenOre.getPlugin().getLogger().warning("No material(s) set for " + sourceBlock);
					continue;
				}
				
				String cPrefix = block.getString("prefix", null);
				Boolean cMultiple = block.getBoolean("dropMultiple", false);
				Boolean cSuppress = block.getBoolean("suppressDrops", false);
				
				// add what blocks should be transformed, if transformation is used.
				ConfigurationSection validTransforms = block.getConfigurationSection("validTransforms");
				List<NamespacedKey> transformThese = new ArrayList<NamespacedKey>();
				if (validTransforms != null) {
					for (String transformL : validTransforms.getKeys(false)) {
						ConfigurationSection transform = validTransforms.getConfigurationSection(transformL);
						String tBlockName = transform.getString("material");
						try {
							Material tBlockMat = Material.getMaterial(tBlockName);
							NamespacedKey tBlockKey = tBlockMat == null ? null : tBlockMat.getKey(); 
							if (tBlockKey != null) {
								transformThese.add(tBlockKey);
							}
						} catch (Exception e) {
							HiddenOre.getPlugin().getLogger().warning("Failed to find valid transform material for " + tBlockName);
							continue;
						}
					}
				} else {
					validTransforms = null;
				}
				
				// we can set up multiple per def, here we handle it.
				for (NamespacedKey cBlockKey : cBlockKeys) {
					BlockConfig bc = new BlockConfig(cBlockKey, cMultiple, cSuppress, cPrefix, new ArrayList<NamespacedKey>(transformThese));
	
					// now add drops.
					ConfigurationSection drops = block.getConfigurationSection("drops");
					if (drops != null) {
						for (String sourceDrop : drops.getKeys(false)) {
							bc.addDropConfig(sourceDrop, grabDropConfig(drops, sourceDrop));
						}
					}
					
					// mix and match in-house drops and "from list" shared drops.
					List<String> dropList = block.getStringList("dropList");
					if (dropList != null) {
						for (String drop : dropList) {
							if (i.dropConfigs.containsKey(drop)) {
								bc.addDropConfig(drop, i.dropConfigs.get(drop));
							}
						}
					}
					
					List<BlockConfig> bclist = worldBlockConfigs.get(cBlockKey);
					if (bclist == null) {
						bclist = new LinkedList<BlockConfig>();
					}
					bclist.add(bc);
	
					worldBlockConfigs.put(cBlockKey, bclist);
				}
			}
		} else {
			HiddenOre.getPlugin().getLogger().info("No blocks specified (Why are you using this plugin?)");
		}

	}
	
	private static DropConfig grabDropConfig(ConfigurationSection drops, String sourceDrop) {
		HiddenOre.getPlugin().getLogger().info("Loading config for drop " + sourceDrop);
		ConfigurationSection drop = drops.getConfigurationSection(sourceDrop);
		String dPrefix = drop.getString("prefix", null);
		@SuppressWarnings("unchecked")
		List<ItemStack> items = (List<ItemStack>) drop.getList("package", new ArrayList<ItemStack>());
		boolean transformIfAble = drop.getBoolean("transformIfAble", false);
		boolean transformDropIfFails = drop.getBoolean("transformDropIfFails", false);
		int transformMaxDropsIfFails = drop.getInt("transformMaxDropsIfFails", 1);
		String command = drop.getString("command", null);
		
		VeinConfig veinNature = null;
		ConfigurationSection veinNatureConfig = drop.getConfigurationSection("veinNature");
		if (veinNatureConfig != null) {
			long densitySeed = veinNatureConfig.getLong("densitySeed", 1);
			long heightSeed = veinNatureConfig.getLong("heightSeed", 2);
			double density = veinNatureConfig.getDouble("density", 1.0);
			double maxSpan = veinNatureConfig.getDouble("maxSpan", 0.0);
			double densityBonus = veinNatureConfig.getDouble("densityBonus", 0.0);
			double areaHeight = veinNatureConfig.getDouble("areaHeight", 1.0);
			double areaSpan = veinNatureConfig.getDouble("areaSpan", 0.0);
			double heightLength = veinNatureConfig.getDouble("heightLength", 1.0);
			double densityLength = veinNatureConfig.getDouble("densityLength", 1.0);
			boolean forceVisible = veinNatureConfig.getBoolean("forceVisibleTransform", false);
			
			veinNature = new VeinConfig(densitySeed, heightSeed, density, maxSpan, densityBonus, areaHeight, areaSpan,
					heightLength, densityLength, forceVisible);
		}

		DropConfig dc = new DropConfig(sourceDrop, DropItemConfig.transform(items), command,
				transformIfAble, transformDropIfFails, transformMaxDropsIfFails,
				dPrefix, grabLimits(drop, new DropLimitsConfig()), veinNature);

		ConfigurationSection biomes = drop.getConfigurationSection("biomes");
		if (biomes != null) {
			for (String sourceBiome : biomes.getKeys(false)) {
				HiddenOre.getPlugin().getLogger().info("Loading config for biome " + sourceBiome);
				DropLimitsConfig dlc = grabLimits(biomes.getConfigurationSection(sourceBiome), dc.limits);
				dc.addBiomeLimits(sourceBiome, dlc);
			}
		}

		return dc;
	}
	
	private static DropLimitsConfig grabLimits(ConfigurationSection drop, DropLimitsConfig parent) {
		DropLimitsConfig dlc = new DropLimitsConfig();
		dlc.setTools(drop.isSet("tools") ? drop.getStringList("tools") : parent.tools);
		dlc.chance = drop.getDouble("chance", parent.chance);
		Double amount = drop.isSet("amount") ? drop.getDouble("amount") : null;
		if (amount != null) {
			dlc.minAmount = amount;
			dlc.maxAmount = amount;
		} else {
			dlc.minAmount = drop.getDouble("minAmount", parent.minAmount);
			dlc.maxAmount = drop.getDouble("maxAmount", parent.maxAmount);
		}
		dlc.minY = drop.getInt("minY", parent.minY);
		dlc.maxY = drop.getInt("maxY", parent.maxY);
		
		// Get xp data as well.
		ConfigurationSection xp = drop.getConfigurationSection("xp");
		if (xp != null) {
			XPConfig xpc = new XPConfig();
			xpc.chance = xp.getDouble("chance", parent.xp != null ? parent.xp.chance : 0.0d);
			Double xpamount = xp.isSet("amount") ? xp.getDouble("amount") : null;
			if (xpamount != null) {
				xpc.minAmount = xpamount;
				xpc.maxAmount = xpamount;
			} else {
				xpc.minAmount = xp.getDouble("minAmount", parent.xp != null ? parent.xp.minAmount : 0.0d);
				xpc.maxAmount = xp.getDouble("maxAmount", parent.xp != null ? parent.xp.maxAmount : 0.0d);
			}
			dlc.xp = xpc;
		}
		
		String state = drop.isSet("state") ? drop.getString("state", parent.state) : parent.state;
		dlc.state = state;
		
		HiddenOre.getPlugin().getLogger()
				.log(Level.INFO, "   loading drop config {0}% {1}-{2} {3}-{4} with {5} tools and {6} state",
						new Object[] {dlc.chance*100.0, dlc.minAmount, dlc.maxAmount, dlc.minY, dlc.maxY, dlc.tools.size(), dlc.state});
		HiddenOre.getPlugin().getLogger().log(Level.INFO, "     tools: {0}", dlc.tools);
		if (dlc.xp != null) {
			HiddenOre.getPlugin().getLogger().log(Level.INFO, "     xp: {0}", dlc.xp.toString());
		}
		return dlc;
	}

	public static BlockConfig isDropBlock(UUID world, BlockData block) {
		List<BlockConfig> bcs = instance.blockConfigs.getOrDefault(world, instance.blockConfigs.get(null)).get(block.getMaterial().getKey());
		if (bcs != null && bcs.size() > 0) {
			// return first match
			return bcs.get(0);
			/*for (BlockConfig bc : bcs) {
				return bc;
			}*/ // TODO 1.13: can we layer in some new distinguishers that could lead to multi-anchoring by Material?
		}
		return null;
	}

	public static String getPrefix(UUID world, BlockData block, String drop) {
		BlockConfig bc = isDropBlock(world, block);
		String pref = (bc == null) ? instance.defaultPrefix : bc.getPrefix(drop);
		return (pref == null ? instance.defaultPrefix : pref);
	}

	public static String getPrefix() {
		return instance.defaultPrefix;
	}

	public static boolean isAlertUser() {
		return instance.alertUser;
	}

	public static boolean isListDrops() {
		return instance.listDrops;
	}

	public static String getPrettyName(String name) {
		NameConfig nc = instance.prettyNames.get(name);
		String pref = (nc == null) ? name : nc.getPrettyName();
		return (pref == null) ? name : pref;
	}

	public static File getTrackFile() {
		return trackFile;
	}
	
	public static boolean isMapActive() {
		return useMapSave;
	}
	
	public static File getMapFile() {
		return mapFile;
	}
	
	public static int getTransformAttemptMultiplier() {
		return instance.transformAttemptMultiplier;
	}
	
	public ConfigurationSection getWorldGenerations() {
		return file.getConfigurationSection("clear_ores");
	}
	
	public static PlayerStateConfig getState(String state) {
		return instance.stateMasterList.get(state);
	}
}
