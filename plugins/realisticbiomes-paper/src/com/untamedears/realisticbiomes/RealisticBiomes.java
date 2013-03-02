package com.untamedears.realisticbiomes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.EntityType;

import com.untamedears.realisticbiomes.listener.GrowListener;
import com.untamedears.realisticbiomes.listener.PlayerListener;
import com.untamedears.realisticbiomes.listener.SpawnListener;

public class RealisticBiomes extends JavaPlugin {

	private static final Logger LOG = Logger.getLogger("RealisticBiomes");
	
	HashMap<Object, GrowthConfig> materialGrowth;

	public void onEnable() {		

		loadConfigs();
		registerEvents();
		
		LOG.info("[RealisticBiomes] is now enabled.");
	}

	private void loadConfigs() {
		// perform check for config file, if it doesn't exist, then create it using the default config file
		if (!this.getConfig().isSet("realistic_biomes")) {
			this.saveDefaultConfig();
			this.getLogger().warning("Config did not exist or was invalid, default config saved.");
		}
		this.reloadConfig();
		
		ConfigurationSection config = this.getConfig().getConfigurationSection("realistic_biomes");
		
		// load names that map to lists of biomes to be used as shorthand for those biomes
		ConfigurationSection biomeAliasSection = config.getConfigurationSection("biome_aliases");
		HashMap<String, List<Biome>> biomeAliases = new HashMap<String, List<Biome>>();
		for (String alias : biomeAliasSection.getKeys(false)) {
			// convert list of strings into list of biomes
			List<String> biomeStrs = biomeAliasSection.getStringList(alias);
			List<Biome> biomes = new ArrayList<Biome>();
			for (String biomeStr : biomeStrs) {
				try {
					Biome biome = Biome.valueOf(biomeStr);
					biomes.add(biome);
				}
				catch(IllegalArgumentException e) {
					LOG.warning("loading configs: in biome_aliases: \"" + biomeStr +"\" is not a valid biome name.");
				}
			}
			
			// map those biomes to an alias
			biomeAliases.put(alias, biomes);
		}
		
		GrowthConfig defaultConfig = new GrowthConfig();
		
		materialGrowth = new HashMap<Object, GrowthConfig>();
		HashMap<String, GrowthConfig> growthConfigNodes = new HashMap<String, GrowthConfig>();
		
		ConfigurationSection growthConfigSection = config.getConfigurationSection("growth");
		for (String materialName : growthConfigSection.getKeys(false)) {
			ConfigurationSection configSection = growthConfigSection.getConfigurationSection(materialName);
			
			GrowthConfig inheritConfig = defaultConfig;
			
			if (configSection.isSet("inherit")) {
				String inheritStr = configSection.getString("inherit");
				
				if (growthConfigNodes.containsKey(inheritStr)) {
					inheritConfig = growthConfigNodes.get(inheritStr);
				}
				else {
					Object inheritKey = getMaterialKey(inheritStr);
					if (materialGrowth.containsKey(inheritKey)) {
						inheritConfig = materialGrowth.get(inheritKey);
					}
				}
			}
			
			GrowthConfig newGrowthConfig = new GrowthConfig(inheritConfig, configSection, biomeAliases);
			
			Object key = getMaterialKey(materialName);	
			if (key == null) {
				// if the name is capitalized, then warnin the player that
				// the name might be a misspelling
				if (materialName.length() > 0 && Character.isUpperCase(materialName.charAt(0)))
					LOG.warning("config material name: is \""+materialName+"\" misspelled?");
				growthConfigNodes.put(materialName, newGrowthConfig);
			}
			else {
				materialGrowth.put(key, newGrowthConfig);
			}
		}
	}
	
	private Object getMaterialKey(String materialName) {
		TreeType treeType;
		try { treeType = TreeType.valueOf(materialName); }
		catch (IllegalArgumentException e) { treeType = null; }
		if (treeType != null)
			return treeType;
		
		Material material = Material.getMaterial(materialName);
		EntityType entityType;
		if (material != null)
			return material;
		
		try { entityType = EntityType.valueOf(materialName); }
		catch (IllegalArgumentException e) { entityType = null; }
		if (entityType != null)
			return entityType;

		return null;
	}
	
	public void onDisable() {
		LOG.info("[RealisticBiomes] is now disabled.");
	}

	private void registerEvents() {
		try {
            PluginManager pm = getServer().getPluginManager();
            pm.registerEvents(new GrowListener(materialGrowth), this);
            pm.registerEvents(new SpawnListener(materialGrowth), this);
            pm.registerEvents(new PlayerListener(materialGrowth), this);
        }
        catch(Exception e)
        {
        	LOG.warning("[RealisticBiomes] caught an exception while attempting to register events with the PluginManager");
        	e.printStackTrace();
        }
	}

}
