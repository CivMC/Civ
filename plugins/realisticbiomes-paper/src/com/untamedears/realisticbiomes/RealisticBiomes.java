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
				// if the name is partially capitalized, then warning the player that
				// the name might be a misspelling
				if (materialName.length() > 0 && materialName.matches(".*[A-Z].*"))
					LOG.warning("config material name: is \""+materialName+"\" misspelled?");
				growthConfigNodes.put(materialName, newGrowthConfig);
			}
			else {
				materialGrowth.put(key, newGrowthConfig);
			}
		}
	}
	
	private Object getMaterialKey(String materialName) {
		boolean isMat = false, isTree = false, isEntity = false;
		// test to see if the material has a "type" specifier, record the specifier and remover it
		if (materialName.startsWith("mat_")) {
			materialName = materialName.replaceFirst("mat\\_", "");
			isMat = true;
		}
		else if (materialName.startsWith("tree_")) {
			materialName = materialName.replaceFirst("tree\\_", "");
			isTree = true;
		}
		else if (materialName.startsWith("entity_")) {
			materialName = materialName.replaceFirst("entity\\_", "");
			isEntity = true;
		}
		
		// match name to bukkit objects
		TreeType treeType;
		try { treeType = TreeType.valueOf(materialName); }
		catch (IllegalArgumentException e) { treeType = null; }
		
		Material material = Material.getMaterial(materialName);
		EntityType entityType;

		try { entityType = EntityType.valueOf(materialName); }
		catch (IllegalArgumentException e) { entityType = null; }
		
		// if the type was specifically specified, thenregister only for that type of object
		// warn if that object doesn't actully match anything
		if (isMat) {
			if (material != null)
				return material;
			LOG.warning("config: \""+materialName+"\" specified material but does not match one.");
		}
		if (isTree) {
			if (treeType != null)
				return treeType;
			LOG.warning("config: \""+materialName+"\" specified tree type name but does not match one.");
		}
		if (isEntity) {
			if (entityType != null)
				return entityType;
			LOG.warning("config: \""+materialName+"\" specified entity type name but does not match one.");
		}
		
		// wanr user if they are unsing an ambiguous name
		if (material != null  && entityType != null && treeType != null)
			LOG.warning("config name: \""+materialName+"\" ambiguous, could be material, tree type, or entity type.");
		if (treeType != null  && material != null)
			LOG.warning("config name: \""+materialName+"\" ambiguous, could be material or tree type.");
		if (material != null  && entityType != null)
			LOG.warning("config name: \""+materialName+"\" ambiguous, could be material or entity type.");
		if (treeType != null  && entityType != null)
			LOG.warning("config name: \""+materialName+"\" ambiguous, could be tree type or entity type.");
		
		// finally just match any type
		if (material != null)
			return material;
		if (treeType != null)
			return treeType;
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
