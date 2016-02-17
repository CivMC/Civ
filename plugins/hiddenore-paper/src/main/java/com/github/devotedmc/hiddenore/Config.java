package com.github.devotedmc.hiddenore;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public final class Config {

	public static Config instance;
	public static boolean isDebug;
	
	public String defaultPrefix;
	public boolean alertUser;
	public boolean listDrops;
	public Map<String, BlockConfig> blockConfigs;
	public Map<String, NameConfig> prettyNames;
	
	private static FileConfiguration file;
	
	private static String trackFileName;
	private static File trackFile;
	public static long trackSave;
	
    private Config() {
    	blockConfigs = new HashMap<String, BlockConfig>();
    	prettyNames = new HashMap<String, NameConfig>();
    	trackFileName = "tracking.dat";
    	trackSave = 90000l;
    	alertUser = false;
    	listDrops = false;
    	isDebug = false;
    	defaultPrefix = "You found hidden ore!";
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
    	
    	i.alertUser = file.getBoolean("alert_user", i.alertUser);
    	i.listDrops = file.getBoolean("list_drops", i.listDrops);
    	i.defaultPrefix = file.getString("prefix", i.defaultPrefix);
    	
    	ConfigurationSection prettyNames = file.getConfigurationSection("pretty_names");
    	if (prettyNames != null) {
	    	for (String key : prettyNames.getKeys(false)) {
	    		NameConfig nc = null;
	    		/* Basically a valid pretty name config can be:
	    		 * 
	    		 * pretty_names:
	    		 *  BUKKIT_NAME: pretty_name
	    		 *  
	    		 * or 
	    		 * 
	    		 * pretty_names:
	    		 *  BUKKIT_NAME:
	    		 *   name: pretty_name
	    		 *   0: subtype_name
	    		 *   1: subtype_name
	    		 * 
	    		 * or any blend.
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
    	
    	ConfigurationSection blocks = file.getConfigurationSection("blocks");
    	if (blocks != null) {
	    	for (String sourceBlock : blocks.getKeys(false)) {
	    		HiddenOre.getPlugin().getLogger().info("Loading config for block " + sourceBlock);
	    		ConfigurationSection block = blocks.getConfigurationSection(sourceBlock);
	 
	    		String cPrefix = block.getString("prefix", null);
	    		Boolean cMultiple = block.getBoolean("dropMultiple", false);
	    		List<Byte> subtypes = (block.getBoolean("allTypes", true)) ? null : block.getByteList("types");
	    		BlockConfig bc = new BlockConfig(subtypes, cMultiple, cPrefix);
	    		
	    		// now add drops.
	    		ConfigurationSection drops = block.getConfigurationSection("drops");
	    		for (String sourceDrop : drops.getKeys(false)) {
	    			HiddenOre.getPlugin().getLogger().info("Loading config for drop " + sourceDrop);
	    			ConfigurationSection drop = drops.getConfigurationSection(sourceDrop);
	    			String dPrefix = drop.getString("prefix", null);
	    			byte dSubtype = (byte) drop.getInt("type", 0);
	    			
	    			DropConfig dc = new DropConfig(dPrefix, dSubtype, grabLimits(drop, new DropLimitsConfig()));
	    			
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
	    		
	    		i.blockConfigs.put(sourceBlock, bc);
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
		Integer amount = drop.isSet("amount") ? drop.getInt("amount") : null;
		if (amount != null) {
			dlc.minAmount = amount;
			dlc.maxAmount = amount;
		} else {
			dlc.minAmount = drop.getInt("minAmount", parent.minAmount);
			dlc.maxAmount = drop.getInt("maxAmount", parent.maxAmount);
		}
		dlc.minY = drop.getInt("minY", parent.minY);
		dlc.maxY = drop.getInt("maxY", parent.maxY);
		HiddenOre.getPlugin().getLogger().log(Level.INFO, "   loading drop config {0}% {1}-{2} {3}-{4}",
				new Object[] {dlc.chance, dlc.minAmount, dlc.maxAmount, dlc.minY, dlc.maxY});
		return dlc;
    }

    public static BlockConfig isDropBlock(String block, byte subtype) {
    	BlockConfig bc = instance.blockConfigs.get(block);
    	if (bc != null) {
    		return bc.checkSubType(subtype) ? bc : null;
    	}
    	return null;
    }

	public static String getPrefix(String block, String drop) {
		BlockConfig bc = instance.blockConfigs.get(block);
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
}
