package com.untamedears.realisticbiomes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;

import com.untamedears.realisticbiomes.listener.GrowListener;
import com.untamedears.realisticbiomes.listener.PlayerListener;
import com.untamedears.realisticbiomes.listener.SpawnListener;
import com.untamedears.realisticbiomes.persist.BlockGrower;
import com.untamedears.realisticbiomes.persist.Coords;
import com.untamedears.realisticbiomes.persist.Plant;
import com.untamedears.realisticbiomes.persist.PlantManager;
import com.untamedears.realisticbiomes.persist.WorldID;

public class RealisticBiomes extends JavaPlugin implements Listener {

	private static final Logger LOG = Logger.getLogger("RealisticBiomes");
	
	public HashMap<Object, GrowthConfig> materialGrowth;
	public BlockGrower blockGrower;
	public PersistConfig persistConfig;
	public PlantManager plantManager;
	
	public void onEnable() {		
		
		WorldID.init(this);
		
		// perform check for config file, if it doesn't exist, then create it using the default config file
		if (!this.getConfig().isSet("realistic_biomes")) {
			this.saveDefaultConfig();
			this.getLogger().warning("Config did not exist or was invalid, default config saved.");
		}
		this.reloadConfig();
		
		ConfigurationSection config = this.getConfig().getConfigurationSection("realistic_biomes");
		
		loadPersistConfig(config);
		loadGrowthConfigs(config);
		
		registerEvents();
		
		plantManager = new PlantManager(this, persistConfig);
		blockGrower = new BlockGrower(plantManager);
		
		getServer().getPluginManager().registerEvents(this, this);
		
		LOG.info("[RealisticBiomes] is now enabled.");
	}

	private void loadPersistConfig(ConfigurationSection config) {
		persistConfig = new PersistConfig();
		
		persistConfig.databaseName = config.getString("file_path");
		persistConfig.unloadBatchPeriod = config.getInt("unload_batch_period");
		persistConfig.growEventLoadChance = config.getDouble("grow_event_load_chance");
	}
	
	private void loadGrowthConfigs(ConfigurationSection config) {
		
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
		LOG.info("[RealisticBiomes] saving plant growth data.");
		plantManager.saveAll();
		plantManager = null;
		LOG.info("[RealisticBiomes] is now disabled.");
	}

	private void registerEvents() {
		try {
            PluginManager pm = getServer().getPluginManager();
            pm.registerEvents(new GrowListener(this, materialGrowth), this);
            pm.registerEvents(new SpawnListener(materialGrowth), this);
            pm.registerEvents(new PlayerListener(this, materialGrowth), this);
        }
        catch(Exception e)
        {
        	LOG.warning("[RealisticBiomes] caught an exception while attempting to register events with the PluginManager");
        	e.printStackTrace();
        }
	}

	public HashMap<Object, GrowthConfig> getGrowthConfigs() {
		return materialGrowth;
	}
	
	public BlockGrower getBlockGrower() {
		return blockGrower;
	}
	
	// -----------------------------------
	
	// grow the specified block, return the new growth magnitude
	public double growAndPersistBlock(Block block, GrowthConfig growthConfig, boolean naturalGrowEvent) {
		int w = WorldID.getPID(block.getWorld().getUID());
		Coords coords = new Coords(w, block.getX(), block.getY(), block.getZ());
		boolean loadChunk = naturalGrowEvent ? Math.random() < persistConfig.growEventLoadChance : true;
		getLogger().info("chunk: "+coords);
		getLogger().info("will load if not loaded: " + loadChunk);
		if (!loadChunk && !plantManager.chunkLoaded(coords))
			return 0.0; // don't load the chunk or do anything
			
		Plant plant = plantManager.get(coords);
		
		if (plant == null) {
			plant = new Plant(System.currentTimeMillis());
			plant.addGrowth((float)BlockGrower.getGrowthFraction(block));
			plantManager.add(coords, plant);
		}
		else {
			double growthAmount = growthConfig.getRate(block) * plant.setUpdateTime(System.currentTimeMillis());
			plant.addGrowth((float) growthAmount);
		}
		
		blockGrower.growBlock(block,coords,plant.getGrowth());
		
		return plant.getGrowth();
	}
	
	public void growBlock(Block block, Coords coords, float growth) {
		block.setData((byte)(7.0*growth));
		
		// if the plant is finished growing, then remove it from the manager
		if (growth >= 1.0) {
			block.setData((byte) 7);
			plantManager.remove(coords);
		}		
	}
	
	public PlantManager getPlantManager() {
		return plantManager;
	}
}
