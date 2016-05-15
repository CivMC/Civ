package com.untamedears.realisticbiomes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.untamedears.realisticbiomes.GrowthConfig.Type;
import com.untamedears.realisticbiomes.listener.GrowListener;
import com.untamedears.realisticbiomes.listener.PlayerListener;
import com.untamedears.realisticbiomes.listener.SpawnListener;
import com.untamedears.realisticbiomes.persist.BlockGrower;
import com.untamedears.realisticbiomes.persist.ChunkCoords;
import com.untamedears.realisticbiomes.persist.Plant;
import com.untamedears.realisticbiomes.persist.PlantManager;
import com.untamedears.realisticbiomes.utils.Fruits;
import com.untamedears.realisticbiomes.utils.MaterialAliases;

public class RealisticBiomes extends JavaPlugin {

	public static RealisticBiomes plugin;

	public static Logger LOG = null;
	public static Level minLogLevel = Level.INFO;
	
	public HashMap<String, List<Biome>> biomeAliases;
	public GrowthMap materialGrowth;
	public GrowthMap fishSpawn;
	public boolean replaceFish;
	public boolean allowTallPlantReplication;
	public BlockGrower blockGrower;
	public PersistConfig persistConfig;
	private PlantManager plantManager;

	
	@Override
	public void onEnable() {		
		RealisticBiomes.plugin = this;
		
		RealisticBiomes.LOG = this.getLogger();
		LOG.info("name of logger is: " + LOG.getName());
		this.getLogger().setLevel(Level.FINEST);
		
		// This is done when the world loads now
		//WorldID.init(this);
		
		// perform check for config file, if it doesn't exist, then create it using the default config file
		if (!this.getConfig().isSet("realistic_biomes")) {
			this.saveDefaultConfig();
			this.getLogger().warning("Config did not exist or was invalid, default config saved.");
			
			// Reload the config into memory from the newly created default file.
			this.reloadConfig();
		}
		
		
		ConfigurationSection config = this.getConfig().getConfigurationSection("realistic_biomes");

		loadBiomeAliases(config);
		loadPersistConfig(config);
		materialGrowth = loadGrowthConfigs(config.getConfigurationSection("growth"), null);
		fishSpawn = loadGrowthConfigs(config.getConfigurationSection("fish_drops"), GrowthConfig.Type.FISHING_DROP);
		replaceFish = config.getBoolean("replace_fishing", false);
		allowTallPlantReplication = config.getBoolean("allow_tallplant_replication", true);

		// load the max log level for our logging hack
		// if not defined then its just initalized at INFO
		String tmp = config.getString("minLogLevel");
		if (tmp != null) {
			Level newLevel = Level.INFO;
			try {
				newLevel = Level.parse(tmp);
			} catch (Exception e) {
				
				// passed in config value is invalid..just set it to info
				newLevel = Level.INFO;
			}
			minLogLevel = newLevel;
		}
		
		RealisticBiomes.LOG.info("Logging hack, log level is set to: " + RealisticBiomes.minLogLevel.toString());
		RealisticBiomes.LOG.info("Caching entire database? " + this.persistConfig.cacheEntireDatabase);
		
		registerEvents();
		
		if (persistConfig.enabled) {
			plantManager = new PlantManager(this, persistConfig);
			blockGrower = new BlockGrower(plantManager, materialGrowth);
			
		}
				
		LOG.info("is now enabled.");
	}
	
	/**
	 * Hack to get around the shitty java logging api, where -Djava.util.logging.config=logging.properties
	 * doesn't seem to work, or i'm not specifying the right levels for the right Logger namespaces or some 
	 * stuff. You specify the min logging level allowed in the realistic biomes config.yml, with the 'minLogLevel' 
	 * key, specifed to a string. Either a Level name like INFO, or an integer
	 * 
	 * This requires that the static Logger variable in RealisticBiomes has already been created (aka onEnable has been called)
	 * @param level
	 * @param message
	 */
	public static void doLog(Level level, String message) {
		
		
		if (RealisticBiomes.LOG != null) {
			
			// here we make sure that we only log messages that are loggable with the given Level
			// so if its set to INFO (800) and we try to log a FINER message (400), then it wont work
			// However if its ALL, then its set to Integer.MIN_VALUE, so everything will get logged. etc etc
			if (level.intValue() >= RealisticBiomes.minLogLevel.intValue() ) {
				RealisticBiomes.LOG.info("[" + level.toString() + "] " + message);
				
			}	
		}
	}

	private void loadPersistConfig(ConfigurationSection config) {
		persistConfig = new PersistConfig();
		
		persistConfig.databaseName = config.getString("database_name");
		persistConfig.host = config.getString("database_host");
		persistConfig.port = config.getString("database_port");
		persistConfig.user = config.getString("database_user");
		persistConfig.password = config.getString("database_password");
		persistConfig.prefix = config.getString("database_prefix");
		
		persistConfig.enabled = config.getBoolean("persistence_enabled");
		persistConfig.unloadBatchPeriod = config.getInt("unload_batch_period");
		persistConfig.unloadBatchMaxTime = config.getInt("unload_batch_max_time");
		persistConfig.growEventLoadChance = config.getDouble("grow_event_load_chance");
		persistConfig.logDB = config.getBoolean("log_db", false);
		
		persistConfig.productionLogDb = config.getBoolean("log_db_production", false);
		persistConfig.productionLogLoadMintime = config.getLong("log_db_production_chunk_load_mintime", 5);
		persistConfig.productionLogUnloadMintime = config.getLong("log_db_production_chunk_unload_mintime", 5);
		
		persistConfig.cacheEntireDatabase = config.getBoolean("cache_entire_database", false);
		
		LOG.info("Persistence enabled: " + persistConfig.enabled);
		LOG.info("Database: " + persistConfig.databaseName);
		if (persistConfig.productionLogDb || persistConfig.logDB) {
			LOG.info("Logging enabled");
			if (persistConfig.productionLogDb) {
				LOG.info("\tLoad event minimum time to log: " + persistConfig.productionLogLoadMintime + "ms");
				LOG.info("\tUnload event minimum time to log: " + persistConfig.productionLogUnloadMintime + "ms");
			}
		}
	}

	private void loadBiomeAliases(ConfigurationSection config) {
		// load names that map to lists of biomes to be used as shorthand for those biomes
		ConfigurationSection biomeAliasSection = config.getConfigurationSection("biome_aliases");
		biomeAliases = new HashMap<String, List<Biome>>();
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
	}
	
	/**
	 * Load growth config into GrowthMap
	 * @param config Configuration section, e.g. "growth"
	 * @param type Force a type or null to guess
	 * @return the growth map
	 */
	private GrowthMap loadGrowthConfigs(ConfigurationSection config, GrowthConfig.Type type) {
		GrowthConfig defaultConfig = new GrowthConfig("default", type);
		GrowthMap materialGrowth = new GrowthMap();
		HashMap<String, GrowthConfig> growthConfigNodes = new HashMap<String, GrowthConfig>();
		
		for (String materialName : config.getKeys(false)) {
			ConfigurationSection configSection = config.getConfigurationSection(materialName);
			
			GrowthConfig inheritConfig = defaultConfig;
			
			if (configSection.isSet("inherit")) {
				String inheritStr = configSection.getString("inherit");
				
				if (growthConfigNodes.containsKey(inheritStr)) {
					inheritConfig = growthConfigNodes.get(inheritStr);
				}
				else {
					Object inheritKey = getMaterialKey(inheritStr);
					if (inheritKey != null) {
						if ((inheritKey instanceof Material) && materialGrowth.containsKey((Material) inheritKey)) {
							inheritConfig = materialGrowth.get((Material) inheritKey);
						} else if ((inheritKey instanceof EntityType) && materialGrowth.containsKey((EntityType) inheritKey)) {
							inheritConfig = materialGrowth.get((EntityType) inheritKey);
						} else if ((inheritKey instanceof TreeType) && materialGrowth.containsKey((TreeType) inheritKey)) {
							inheritConfig = materialGrowth.get((TreeType) inheritKey);
						} else {
							LOG.warning(configSection.getName() + " inherits unknown key: " + inheritKey);
							LOG.finest("keys: " + materialGrowth.keySet());
						}
					}
				}
			}
			
			GrowthConfig newGrowthConfig = GrowthConfig.get(materialName, (GrowthConfig)inheritConfig, configSection, biomeAliases);
			
			Object key = getMaterialKey(materialName);
			
			newGrowthConfig.setName(key);
			
			if (key == null) {
				// if the name is partially capitalized, then warning the player that
				// the name might be a misspelling
				if (materialName.length() > 0 && materialName.matches(".*[A-Z].*"))
					LOG.warning("config material name: is \""+materialName+"\" misspelled?");
				growthConfigNodes.put(materialName, newGrowthConfig);

			} else if (key instanceof Material){
				materialGrowth.put((Material)key, newGrowthConfig, type);
			} else if (key instanceof EntityType){
				materialGrowth.put((EntityType)key, newGrowthConfig);
			} else if (key instanceof TreeType){
				materialGrowth.put((TreeType)key, newGrowthConfig);
			} else {
				LOG.warning("unknown key: " + key);
			}
		}
		
		return materialGrowth;
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
	
	@Override
	public void onDisable() {
		if (persistConfig.enabled) {
			LOG.info("saving plant growth data.");
			plantManager.saveAllAndStop();
			plantManager = null;
		}
		LOG.info("is now disabled.");
	}

	private void registerEvents() {
		try {
			PluginManager pm = getServer().getPluginManager();
			pm.registerEvents(new GrowListener(this), this);
			pm.registerEvents(new SpawnListener(materialGrowth, fishSpawn), this);
			pm.registerEvents(new PlayerListener(this, materialGrowth), this);
		}
		catch(Exception e)
		{
			LOG.severe("caught an exception while attempting to register events with the PluginManager: " + e);
		}
	}
	
	// -----------------------------------
	
	/**
	 * grow the specified block, return the new growth magnitude
	 * @param block The block to grow
	 * @param naturalGrowEvent False if from player interaction (stick/block hit info)
	 * @param growthConfig Growth config, will be looked up if null
	 * @param fruitBlockToIgnore When checking for fruits, ignore this block. BlockBreak event needs this, since the block being broken is still in world until event has completed.
	 * @return Plant or null, to report growth back to player on interaction
	 */
	public Plant growAndPersistBlock(Block block, boolean naturalGrowEvent, GrowthConfig growthConfig, Block fruitBlockToIgnore, DropGrouper dropGrouper) {
		if (growthConfig == null) {
			growthConfig = MaterialAliases.getConfig(materialGrowth, block);
		}
		RealisticBiomes.doLog(Level.FINER, "RealisticBiomes:growAndPersistBlock() called for block: " + block + " and is naturalGrowEvent? " + naturalGrowEvent);
		if (!persistConfig.enabled)
			return null;

		ChunkCoords chunckCoords = new ChunkCoords(block.getChunk()); 
		
		boolean loadChunk = naturalGrowEvent ? Math.random() < persistConfig.growEventLoadChance : true;
		if (!loadChunk && !plantManager.isChunkLoaded(chunckCoords)) {
			RealisticBiomes.doLog(Level.FINER, "Realisticbiomes.growAndPersistBlock(): returning 0.0 because loadChunk = false or plantManager.chunkLoaded(" + chunckCoords + " is false");
			return null; // don't load the chunk or do anything
			
		}
			
		Plant plant = plantManager.getPlantFromBlock(block);
		
		
		if (growthConfig == null) {
			RealisticBiomes.doLog(Level.FINER, "Realisticbiomes.growAndPersistBlock(): returning 0.0 because growthConfig = null");
			plantManager.removePlant(block);
			return null;
		}
		
		// Only persistent crops should be grown in this manner
		if (!growthConfig.isPersistent()) {
			return null;
		}
		
		if (plant == null) {
			RealisticBiomes.doLog(Level.FINER, "Realisticbiomes.growAndPersistBlock(): creating new plant and adding it");
			
			plant = new Plant((float)BlockGrower.getGrowthFraction(block),
					(float)BlockGrower.getFruitGrowthFraction(block, fruitBlockToIgnore));
			plantManager.addPlant(block, plant);
		}
		
		growPlant(plant, block, growthConfig, fruitBlockToIgnore, dropGrouper);
		
		if (plant.isFullyGrown()) {
			// if plant is fully grown and either has no fruits or fruit has fully grown, stop tracking it
			plantManager.removePlant(block);
		}
		
		return plant;
	}
	
	/**
	 * Grow the plant
	 * If not `fruitBlockToIgnore` is not null, this comes from BlockBreak. The event needs this, since
	 * the block being broken is still in world until event has completed.
	 * @param plant
	 * @param block
	 * @param growthConfig
	 * @param fruitBlockToIgnore When checking for fruits, ignore this block
	 * @param dropGrouper 
	 */
	public void growPlant(Plant plant, Block block, GrowthConfig growthConfig, Block fruitBlockToIgnore, DropGrouper dropGrouper) {
		double rate = growthConfig.getRate(block);
		double fruitRate = -1.0;
		
		double updateTime = plant.grow(rate);
		
		if (Fruits.isFruitFul(block.getType())) {
			boolean hasFruit = Fruits.hasFruit(block, fruitBlockToIgnore);
			GrowthConfig fruitGrowthConfig = materialGrowth.get(Fruits.getFruit(block.getType()));
			if (fruitGrowthConfig.isPersistent()) {	
				if (!hasFruit && plant.getGrowth() >= 1.0) {	
					if (plant.getFruitGrowth() == -1.0) {
						// first time a stem is fully grown, reset fruit to zero
						plant.setFruitGrowth(0.0f);
					}
					
					Block freeBlock = Fruits.getFreeBlock(block, fruitBlockToIgnore);
					if (freeBlock != null) {
						// got a free spot, now grow a fruit there with the fruit's conditions
						fruitRate = fruitGrowthConfig.getRate(freeBlock);
						
						RealisticBiomes.doLog(Level.FINER, "Realisticbiomes.growPlant(): fruit rate: " + fruitRate);
						
						plant.growFruit(updateTime, fruitRate);
					} else {
						RealisticBiomes.doLog(Level.FINER, "Realisticbiomes.growPlant(): no free block for fruit");
					}

				} else if (hasFruit) {
					plant.setFruitGrowth(0.0f);
				}
			}
		}
		
		// actually 'grows' the block or fruit (in minecraft terms, between the different stages of growth that you can see in game)
		// depending on its growth value
		boolean growthPrevented = false;
		if (growthConfig.getType() == Type.TREE) {
			growthPrevented = blockGrower.generateTree(block, plant.getGrowth(), growthConfig.getTreeType());
		} else if (growthConfig.getType() == Type.COLUMN) {
			growthPrevented = blockGrower.growColumn(block, plant.getGrowth(), dropGrouper);
		} else {
			growthPrevented = blockGrower.growBlock(block, plant.getGrowth(), plant.getFruitGrowth());
		}
		if (growthPrevented) {
			RealisticBiomes.doLog(Level.FINER, "Realisticbiomes.growPlant(): growth prevented");
			plant.setGrowth(0.0);
		}
	}
	
	public PlantManager getPlantManager() {
		return plantManager;
	}
}
