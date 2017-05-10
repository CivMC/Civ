package com.github.devotedmc.hiddenore;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public final class Config {

	public static Config instance;
	public static boolean isDebug;

	public String defaultPrefix;
	public boolean alertUser;
	public boolean listDrops;
	public boolean ignoreSilktouch;
	public Map<String, List<BlockConfig>> blockConfigs;
	public Map<String, NameConfig> prettyNames;

	private static FileConfiguration file;

	private static String trackFileName;
	private static File trackFile;
	public static long trackSave;
	
	public int transformAttemptMultiplier = 3;

	private Config() {
		blockConfigs = new HashMap<String, List<BlockConfig>>();
		prettyNames = new HashMap<String, NameConfig>();
		trackFileName = "tracking.dat";
		trackSave = 90000l;
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

		trackFileName = file.getString("track_file", trackFileName);
		trackFile = new File(HiddenOre.getPlugin().getDataFolder(), trackFileName);
		trackSave = file.getLong("track_save_ticks", trackSave);

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
				 * Basically a valid pretty name config can be: pretty_names: BUKKIT_NAME: pretty_name or pretty_names:
				 * BUKKIT_NAME: name: pretty_name 0: subtype_name 1: subtype_name or any blend.
				 */
				if (prettyNames.isConfigurationSection(key)) {
					ConfigurationSection pName = prettyNames.getConfigurationSection(key);
					String name = pName.getString("name", key);
					nc = new NameConfig(name);
					for (String subtype : pName.getKeys(false)) {
						if (!subtype.equals("name")) {
							try {
								nc.addSubTypePrettyName(Short.parseShort(subtype), pName.getString(subtype, name));
							} catch (NumberFormatException nfe) {
								HiddenOre.getPlugin().getLogger().info(subtype + " is not a valid subtype for " + key);
							}
						}
					}
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

		ConfigurationSection blocks = file.getConfigurationSection("blocks");
		if (blocks != null) {
			for (String sourceBlock : blocks.getKeys(false)) {
				HiddenOre.getPlugin().getLogger().info("Loading config for " + sourceBlock);
				ConfigurationSection block = blocks.getConfigurationSection(sourceBlock);

				String cBlockName = block.getString("material");
				if (cBlockName == null) {
					HiddenOre.getPlugin().getLogger().warning("Failed to find material for " + sourceBlock);
					continue;
				}
				String cPrefix = block.getString("prefix", null);
				Boolean cMultiple = block.getBoolean("dropMultiple", false);
				Boolean cSuppress = block.getBoolean("suppressDrops", false);
				List<Byte> subtypes = (block.getBoolean("allTypes", true)) ? null : block.getByteList("types");
				
				// add what blocks should be transformed, if transformation is used.
				ConfigurationSection validTransforms = block.getConfigurationSection("validTransforms");
				List<BlockConfig.MaterialWrapper> transformThese = new ArrayList<BlockConfig.MaterialWrapper>();
				if (validTransforms != null) {
					for (String transformL : validTransforms.getKeys(false)) {
						ConfigurationSection transform = validTransforms.getConfigurationSection(transformL);
						String tBlockName = transform.getString("material");
						List<Byte> tSubtypes = (transform.getBoolean("allTypes", true)) ? null : transform.getByteList("types");
						transformThese.add(new BlockConfig.MaterialWrapper(tBlockName, tSubtypes));
					}
				} else {
					validTransforms = null;
				}
				BlockConfig bc = new BlockConfig(cBlockName, subtypes, cMultiple, cSuppress, cPrefix, transformThese);

				// now add drops.
				ConfigurationSection drops = block.getConfigurationSection("drops");
				for (String sourceDrop : drops.getKeys(false)) {
					HiddenOre.getPlugin().getLogger().info("Loading config for drop " + sourceDrop);
					ConfigurationSection drop = drops.getConfigurationSection(sourceDrop);
					String dPrefix = drop.getString("prefix", null);
					@SuppressWarnings("unchecked")
					List<ItemStack> items = (List<ItemStack>) drop.getList("package");
					boolean transformIfAble = drop.getBoolean("transformIfAble", false);
					boolean transformDropIfFails = drop.getBoolean("transformDropIfFails", false);
					int transformMaxDropsIfFails = drop.getInt("transformMaxDropsIfFails", 1);

					DropConfig dc = new DropConfig(sourceDrop, DropItemConfig.transform(items), 
							transformIfAble, transformDropIfFails, transformMaxDropsIfFails,
							dPrefix, grabLimits(drop, new DropLimitsConfig()));

					ConfigurationSection biomes = drop.getConfigurationSection("biomes");
					if (biomes != null) {
						for (String sourceBiome : biomes.getKeys(false)) {
							HiddenOre.getPlugin().getLogger().info("Loading config for biome " + sourceBiome);
							DropLimitsConfig dlc = grabLimits(biomes.getConfigurationSection(sourceBiome), dc.limits);
							dc.addBiomeLimits(sourceBiome, dlc);
						}
					}

					bc.addDropConfig(sourceDrop, dc);
				}
				List<BlockConfig> bclist = i.blockConfigs.get(cBlockName);//sourceBlock);
				if (bclist == null) {
					bclist = new LinkedList<BlockConfig>();
				}
				bclist.add(bc);

				i.blockConfigs.put(cBlockName, bclist);//sourceBlock, bclist);
			}
		} else {
			HiddenOre.getPlugin().getLogger().info("No blocks specified (Why are you using this plugin?)");
		}

		instance = i;
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
		
		HiddenOre.getPlugin().getLogger()
				.log(Level.INFO, "   loading drop config {0}% {1}-{2} {3}-{4} with {5} tools",
						new Object[] {dlc.chance*100.0, dlc.minAmount, dlc.maxAmount, dlc.minY, dlc.maxY, dlc.tools.size()});
		HiddenOre.getPlugin().getLogger().log(Level.INFO, "     tools: {0}", dlc.tools);
		if (dlc.xp != null) {
			HiddenOre.getPlugin().getLogger().log(Level.INFO, "     xp: {0}", dlc.xp.toString());
		}
		return dlc;
	}

	public static BlockConfig isDropBlock(String block, byte subtype) {
		List<BlockConfig> bcs = instance.blockConfigs.get(block);
		if (bcs != null && bcs.size() > 0) {
			// return first match
			for (BlockConfig bc : bcs) {
				if (bc.checkSubType(subtype)) {
					return bc;
				}
			}
		}
		return null;
	}

	public static String getPrefix(String block, byte subtype, String drop) {
		BlockConfig bc = isDropBlock(block, subtype);
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

	public static String getPrettyName(String name, short durability) {
		NameConfig nc = instance.prettyNames.get(name);
		String pref = (nc == null) ? name : nc.getSubTypePrettyName(durability);
		return (pref == null) ? name : pref;
	}

	public static File getTrackFile() {
		return trackFile;
	}
	
	public static int getTransformAttemptMultiplier() {
		return instance.transformAttemptMultiplier;
	}
	
	public ConfigurationSection getWorldGenerations() {
		return file.getConfigurationSection("clear_ores");
	}
}
