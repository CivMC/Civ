package com.github.igotyou.FactoryMod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.igotyou.FactoryMod.FactoryObject.FactoryType;
import com.github.igotyou.FactoryMod.Factorys.ProductionFactory;
import com.github.igotyou.FactoryMod.listeners.FactoryModListener;
import com.github.igotyou.FactoryMod.listeners.NoteStackListener;
import com.github.igotyou.FactoryMod.listeners.RedstoneListener;
import com.github.igotyou.FactoryMod.managers.FactoryManagerService;
import com.github.igotyou.FactoryMod.managers.ProductionFactoryManager;
import com.github.igotyou.FactoryMod.properties.IFactoryProperties;
import com.github.igotyou.FactoryMod.properties.CompactorProperties;
import com.github.igotyou.FactoryMod.properties.NetherFactoryProperties;
import com.github.igotyou.FactoryMod.properties.PrintingPressProperties;
import com.github.igotyou.FactoryMod.properties.ProductionProperties;
import com.github.igotyou.FactoryMod.properties.RepairFactoryProperties;
import com.github.igotyou.FactoryMod.recipes.EnchantmentOptions;
import com.github.igotyou.FactoryMod.recipes.ProbabilisticEnchantment;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


public class FactoryModPlugin extends JavaPlugin
{
	/* Special Values */
	public static final String VERSION = "v1.4.0"; //Current version of plugin
	public static final String PLUGIN_NAME = "FactoryMod"; //Name of plugin
	public static final String PLUGIN_PREFIX = PLUGIN_NAME + " " + VERSION + ": ";
	public static final String PRINTING_PRESSES_SAVE_FILE = "pressSaves"; // The printing press saves file name
	public static final String NETHER_FACTORY_SAVE_FILE = "netherSaves"; // The nether saves file name
	public static final String REPAIR_FACTORY_SAVE_FILE = "repairSaves";
	public static final String COMPACTOR_SAVE_FILE = "compactorSaves";
	public static final String PRODUCTION_FACTORY_SAVE_FILE = "productionSaves";
	public static final String PERSISTENCE_FORMAT = "txt";
	public static final int TICKS_PER_SECOND = 20; //ideal number of ticks per second
	public static final int TICKS_PER_MIN = 20; //ideal number of ticks per minute
	public static final int MILLIS_PER_DAY =  24 * 60 * 60 * 1000; // number of milliseconds per day (86.4M)
	public static final int MINUTES_PER_YEAR = 60 * 60 * 24 * 365; //number of minutes per year (31.5M)
	private static final String CONFIG_FILE = "config.yml"; // the config file name
	private static final NamedItemStack DEFAULT_FUEL = new NamedItemStack(Material.COAL, 1, (short)1, "Charcoal"); // The default fuel item
	
	/* General Properties */

	/**
	 * How frequently in ticks factory states are updated (Default 20)
	 */
	public static int PRODUCER_UPDATE_CYCLE;
	/**
	 * How often in minutes the managers should save their factories to file (Default 15)
	 */
	public static int SAVE_CYCLE;
	/**
	 * Number of days after a factory falls into disrepair until it is removed (Default 14)
	 */
	public static long DISREPAIR_PERIOD;
	/**
	 * 	Number of days for a factory to fully degrade from 100% to 0% health (Default 28)
	 */
	public static long REPAIR_PERIOD;
	/**
	 * Factory center block material (Default WORKBENCH)
	 */
	public static Material CENTRAL_BLOCK_MATERIAL;
	/**
	 * The item used to interact with factoies (Default STICK)
	 */
	public static Material FACTORY_INTERACTION_MATERIAL;
	/**
	 * Whether the build materials are returned upon destruction of a factory (Default false)
	 */
	public static boolean RETURN_BUILD_MATERIALS;
	/**
	 * Whether Citadel is enabled (Default true)
	 */
	public static boolean CITADEL_ENABLED;
	/**
	 * Whether factories are permanently removed upon the destruction of their blocks (Default false)
	 */
	public static boolean DESTRUCTIBLE_FACTORIES;
	/**
	 * Whether XP orb drops are disabled (Default false)
	 */
	public static boolean DISABLE_EXPERIENCE;
	/**
	 * Whether factories are triggered by redstone signals (Default true)
	 */
	public static boolean REDSTONE_START_ENABLED;
	/**
	 * Whether factory running state is output to a lever (Default true)
	 */
	public static boolean LEVER_OUTPUT_ENABLED;
	
	/* Nether Properties */
	
	/**
	 * The name of the overworld dimension (Default world)
	 */
	public static String WORLD_NAME;
	/**
	 * The name of the nether dimension (Default world_nether)
	 */
	public static String NETHER_NAME;
	/**
	 * Overworld to nether distance ratio (Default 8)
	 */
	public static int NETHER_SCALE;
	/**
	 * Disable vanilla nether portal operation (Default true)
	 */
	public static boolean DISABLE_PORTALS;
	/**
	 * The material of the nether factory teleport platform (Default OBSIDIAN)
	 */
	public static Material NETHER_FACTORY_TELEPORT_PLATFORM_MATERIAL;
	/**
	 * Allow reinforcement above nether factory teleport platforms (Default false)
	 */
	public static boolean ALLOW_REINFORCEMENT_CREATION_ABOVE_TELEPORT_PLATFORM;
	/**
	 * Allow placing of blocks above nether factory teleport platform (Default false)
	 */
	public static boolean ALLOW_BLOCK_PLACEMENT_ABOVE_TELEPORT_PLATFORM;
	/**
	 * Whether teleport platforms are unbreakable (Default false)
	 */
	public static boolean TELEPORT_PLATFORM_INVUNERABLE;
	/**
	 * Whether the teleport platform is generated when a player is about to teleport (Default false)
	 */
	public static boolean REGENERATE_TELEPORT_BLOCK_ON_TELEPORT;
	/**
	 * Whether the blocks above a portal are destroyed, ignoring Citadel, when a player is about to teleport (Default false)
	 */
	public static boolean REMOVE_BLOCK_ABOVE_TELEPORT_PLATFORM_ON_TELEPORT;
	/**
	 * The material of the nether factory marker block (Default COAL_BLOCK)
	 */
	public static Material NETHER_FACTORY_MARKER_MATERIAL;	
	/**
	 * Maximum distance between nether factory and marker (Default 64)
	 */
	public static int NETHER_MARKER_MAX_DISTANCE;
	
	public static Map<String, ProductionProperties> productionProperties;
	public static Map<String,ProductionRecipe> productionRecipes;
	
	public FactoryManagerService manager;
	public PrintingPressProperties printingPressProperties;
	public NetherFactoryProperties netherFactoryProperties;
	public RepairFactoryProperties repairFactoryProperties;
	public CompactorProperties compactorProperties;
	
	public void onEnable()
	{
		plugin = this;
		//load the config.yml
		initConfig();
		//create the main manager
		manager = new FactoryManagerService(this);
		//register the events(this should be moved...)
		registerEvents();
	}
	
	public void onDisable()
	{
		//call the disable method, this will save the data etc.
		manager.onDisable();
	}
	
	public void registerEvents()
	{
		try
		{
			getServer().getPluginManager().registerEvents(new FactoryModListener(manager), this);
			getServer().getPluginManager().registerEvents(new RedstoneListener(manager), this);
			getServer().getPluginManager().registerEvents(new NoteStackListener(this), this);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void initConfig()
	{
		sendConsoleMessage("Initializing FactoryMod Config.");
		
		productionProperties = Maps.newHashMap();
		productionRecipes = Maps.newHashMap();
		
		FileConfiguration config = getConfig();
		if(getConfig().getDefaults().getBoolean("copy_defaults", true))
		{
			saveResource(CONFIG_FILE, true);
		}
		this.saveDefaultConfig();
		reloadConfig();
		config = getConfig();
		//what should the nether scaling be for the nether factorys?
		NETHER_SCALE = config.getInt("nether_general.nether_scale",8);
		//Should we Disable regular portals?
		DISABLE_PORTALS = config.getBoolean("nether_general.disable_portals", true);
		//Allow reinforcement above nether factory teleport platforms.
		ALLOW_REINFORCEMENT_CREATION_ABOVE_TELEPORT_PLATFORM = config.getBoolean("nether_general.allow_reinforcement_creation_above_teleport_platform", false);
		//Allow people to place blocks above nether factory teleport platforms.
		ALLOW_BLOCK_PLACEMENT_ABOVE_TELEPORT_PLATFORM = config.getBoolean("nether_general.allow_block_placement_above_teleport_platform", false);
		//Make teleport platforms unbreakable
		TELEPORT_PLATFORM_INVUNERABLE = config.getBoolean("nether_general.teleport_platform_invunerable",false);
		//Right before a player get's teleported, should the teleport platform be regenerated?
		REGENERATE_TELEPORT_BLOCK_ON_TELEPORT = config.getBoolean("nether_general.regenerate_teleport_block_on_teleport", false);
		//Right before a player get's teleported, should the blocks above the portal be destroyed(ignotes citadel)?
		REMOVE_BLOCK_ABOVE_TELEPORT_PLATFORM_ON_TELEPORT = config.getBoolean("nether_general.remove_blocks_above_teleport_platform_on_teleport", false);
		//what's the name of the overworld?
		WORLD_NAME = config.getString("nether_general.world_name", "world");
		//what's the name of the overworld?
		NETHER_NAME = config.getString("nether_general.nether_name", "world_nether");
		//how often should the managers save?
		SAVE_CYCLE = config.getInt("general.save_cycle",15)*60*20;
		//what's the material of the center block of factorys?
		CENTRAL_BLOCK_MATERIAL = Material.getMaterial(config.getString("general.central_block"));
		//what's the material of the nether portal teleportation platforms?
		NETHER_FACTORY_TELEPORT_PLATFORM_MATERIAL = Material.getMaterial(config.getString("nether_general.teleport_platform_material_nether_factory"));
		//what's the material of the marker blocks for nether factorys?
		NETHER_FACTORY_MARKER_MATERIAL = Material.getMaterial(config.getString("nether_general.marker_material_nether_factory"));
		//how far from the factory can the marker be?
		NETHER_MARKER_MAX_DISTANCE = config.getInt("nether_general.marker_max_distance");
		//Return the build materials upon destruction of factory.
		RETURN_BUILD_MATERIALS = config.getBoolean("general.return_build_materials",false);
		//is citadel enabled?
		CITADEL_ENABLED = config.getBoolean("general.citadel_enabled",true);
		//what's the tool that we use to interact with the factorys?
		FACTORY_INTERACTION_MATERIAL = Material.getMaterial(config.getString("general.factory_interaction_material","STICK"));
		//If factories are removed upon destruction of their blocks
		DESTRUCTIBLE_FACTORIES=config.getBoolean("general.destructible_factories",false);		
		//Check if XP drops should be disabled
		DISABLE_EXPERIENCE=config.getBoolean("general.disable_experience",false);
		//How frequently factories are updated
		PRODUCER_UPDATE_CYCLE = config.getInt("production_general.update_cycle",20);
		//Period of days before a factory is removed after it falls into disrepair
		DISREPAIR_PERIOD= config.getLong("general.disrepair_period",14)*24*60*60*1000;
		//The length of time it takes a factory to go to 0% health
		REPAIR_PERIOD = config.getLong("production_general.repair_period",28)*24*60*60*1000;
		//Disable recipes which result in the following items
		//Do we output the running state with a lever?
		LEVER_OUTPUT_ENABLED = config.getBoolean("general.lever_output_enabled",true);
		//Do we allow factories to be started with redstone?
		REDSTONE_START_ENABLED = config.getBoolean("general.redstone_start_enabled",true);

		// Disable the following recipes
		List<Recipe> toDisable = new ArrayList<Recipe>();
		ItemList<NamedItemStack> disabledRecipes = getItems(config.getConfigurationSection("crafting.disable"));		
		for (NamedItemStack recipe : disabledRecipes) {
			sendConsoleMessage("Attempting to disable recipes for " + recipe.getCommonName());
			
			List<Recipe> tempList = getServer().getRecipesFor(recipe);
			
			for (Recipe potential : tempList) {
				if (potential.getResult().isSimilar(recipe)) {
					sendConsoleMessage("Found a disable recipe match " + potential.toString());
					toDisable.add(potential);
				}
			}
		}

		Iterator<Recipe> it = getServer().recipeIterator();
		while (it.hasNext()) {
			Recipe recipe = it.next();
			for (Recipe disable : toDisable) { // why can't they just override equality tests?!
				if (disable.getResult().isSimilar(recipe.getResult())) {
					it.remove();
					sendConsoleMessage("Disabling recipe " + recipe.getResult().toString());
				}
			}
		}
				
		//Enable the following recipes
		ConfigurationSection configCraftingEnable = config.getConfigurationSection("crafting.enable");
		for (String recipeName : configCraftingEnable.getKeys(false))
		{
			ConfigurationSection configSection=configCraftingEnable.getConfigurationSection(recipeName);
			Recipe recipe;
			List<String> shape = configSection.getStringList("shape");

			NamedItemStack output = getItems(configSection.getConfigurationSection("output")).get(0);
			
			if(shape.isEmpty())
			{
				ShapelessRecipe shapelessRecipe = new ShapelessRecipe(output);
				
				for (ItemStack input:getItems(configSection.getConfigurationSection("inputs")))
				{
					shapelessRecipe.addIngredient(input.getAmount(), input.getType());
					//shapelessRecipe.addIngredient(input.getAmount(), input.getType(), input.getDurability());
				}
				
				recipe = shapelessRecipe;
				sendConsoleMessage("Enabling shapeless recipe " + recipeName + " - " + recipe.getResult().getType().name());
			}
			else
			{
				for (String line : shape) {
					sendConsoleMessage("New Shape Line: [" + line + "]");
				}
				
				sendConsoleMessage("output: " + output.toString());
				
				ShapedRecipe shapedRecipe = new ShapedRecipe(output);
				shapedRecipe.shape(shape.toArray(new String[shape.size()]));
				
				for(String inputKey : configSection.getConfigurationSection("inputs").getKeys(false))
				{
					ItemStack input = getItems(configSection.getConfigurationSection("inputs." + inputKey)).get(0);
					shapedRecipe.setIngredient(inputKey.charAt(0),input.getType());
					//shapedRecipe.setIngredient(inputKey.charAt(0),input.getType(),input.getDurability());
				}
				
				recipe = shapedRecipe;
				sendConsoleMessage("Enabling shaped recipe " + recipeName + " - " + recipe.getResult().getType().name());
			}
			
			Bukkit.addRecipe(recipe);
		}
		
		//Import recipes from config.yml
		ConfigurationSection configProdRecipes = config.getConfigurationSection("production_recipes");
		//Temporary Storage array to store where recipes should point to each other
		Map<ProductionRecipe, List<String>> outputRecipes = Maps.newHashMap();
		
		for (String title : configProdRecipes.getKeys(false))
		{
			//Section header in recipe file, also serves as unique identifier for the recipe
			//All spaces are replaced with udnerscores so they don't disrupt saving format
			//There should be a check for uniqueness of this identifier...
			ConfigurationSection configSection = configProdRecipes.getConfigurationSection(title);
			title = title.replaceAll(" ", "_");
			//Display name of the recipe, Deafult of "Default Name"
			String recipeName = configSection.getString("name", "Default Name");
			//Production time of the recipe, default of 1
			//TODO: fix config according to default 1
			int productionTime = configSection.getInt("production_time", 2);
			//Inputs of the recipe, empty of there are no inputs
			ItemList<NamedItemStack> inputs = getItems(configSection.getConfigurationSection("inputs"));
			//Inputs of the recipe, empty of there are no inputs
			ItemList<NamedItemStack> upgrades = getItems(configSection.getConfigurationSection("upgrades"));
			//Outputs of the recipe, empty of there are no inputs
			ItemList<NamedItemStack> outputs = getItems(configSection.getConfigurationSection("outputs"));
			//EnchantmentOptions of the recipe, all false if nothing set.
			ConfigurationSection configEnchant = configSection.getConfigurationSection("enchantment_options");
			EnchantmentOptions enchantmentOptions = null;
			if (configEnchant != null) {
				enchantmentOptions = new EnchantmentOptions(configEnchant.getBoolean("safe_only", false), 
						configEnchant.getBoolean("ensure_one", false));
			} else {
				enchantmentOptions = EnchantmentOptions.DEFAULT;
			}
			//Enchantments of the recipe, empty of there are no inputs
			List<ProbabilisticEnchantment> enchantments = getEnchantments(configSection.getConfigurationSection("enchantments"));
			//Whether this recipe can only be used once
			boolean useOnce = configSection.getBoolean("use_once");
			ProductionRecipe recipe = new ProductionRecipe(title, recipeName, productionTime, inputs, upgrades, outputs, enchantmentOptions, enchantments, useOnce, new ItemList<NamedItemStack>());
			productionRecipes.put(title, recipe);
			//Store the titles of the recipes that this should point to
			List <String> currentOutputRecipes = Lists.newArrayList();
			currentOutputRecipes.addAll(configSection.getStringList("output_recipes"));
			outputRecipes.put(recipe, currentOutputRecipes);
		}
		
		//Once ProductionRecipe objects have been created correctly insert different pointers
		for (ProductionRecipe recipe : outputRecipes.keySet())
		{
			for( String output : outputRecipes.get(recipe))
			{
				recipe.addOutputRecipe(productionRecipes.get(output));
			}
		}
		
		
		//Import factories from config.yml
		ConfigurationSection configProdFactories = config.getConfigurationSection("production_factories");
		for (String title : configProdFactories.getKeys(false))
		{
			ConfigurationSection configSection = configProdFactories.getConfigurationSection(title);
			title = title.replaceAll(" ", "_");
			String factoryName = configSection.getString("name", "Default Name");
			//Uses overpowered getItems method for consistency, should always return a list of size=1
			//If no fuel is found, default to charcoal
			ItemList<NamedItemStack> fuel = getItems(configSection.getConfigurationSection("fuel"));
			if(fuel.isEmpty())
			{
				fuel = new ItemList<NamedItemStack>();
				fuel.add(DEFAULT_FUEL);
			}
			//TODO: default fuel time should be 1
			int fuelTime = configSection.getInt("fuel_time", 2);
			ItemList<NamedItemStack> inputs = getItems(configSection.getConfigurationSection("inputs"));
			ItemList<NamedItemStack> repairs = getItems(configSection.getConfigurationSection("repair_inputs"));
			
			List<ProductionRecipe> factoryRecipes = Lists.newArrayList();
			for (String recipe : configSection.getStringList("recipes"))
			{
				factoryRecipes.add(productionRecipes.get(recipe));
			}
			
			int repair = configSection.getInt("repair_multiple",0);
			//Create repair recipe
			productionRecipes.put(title + "REPAIR", new ProductionRecipe(title + "REPAIR", "Repair Factory", 1, repairs));
			factoryRecipes.add(productionRecipes.get(title + "REPAIR"));
			ProductionProperties productionProperty = new ProductionProperties(inputs, factoryRecipes, fuel, fuelTime, factoryName, repair);
			productionProperties.put(title, productionProperty);
		}
		
		ConfigurationSection configPrintingPresses = config.getConfigurationSection("printing_presses");
		ConfigurationSection configNetherFactory = config.getConfigurationSection("nether_factory");
		ConfigurationSection configRepairFactory=config.getConfigurationSection("repair_factory");
		ConfigurationSection configCompactor=config.getConfigurationSection("compactor");
		printingPressProperties = PrintingPressProperties.fromConfig(this, configPrintingPresses);
		netherFactoryProperties = NetherFactoryProperties.fromConfig(this, configNetherFactory);
		repairFactoryProperties = RepairFactoryProperties.fromConfig(this, configRepairFactory);
		compactorProperties = CompactorProperties.fromConfig(this, configCompactor);
		sendConsoleMessage("Finished initializing FactoryMod Config.");
	}
	
	private List<ProbabilisticEnchantment> getEnchantments(ConfigurationSection configEnchantments)
	{
		List<ProbabilisticEnchantment> enchantments = Lists.newArrayList();
		if(configEnchantments != null)
		{
			for (String name : configEnchantments.getKeys(false))
			{
				ConfigurationSection configEnchantment = configEnchantments.getConfigurationSection(name);
				String type = configEnchantment.getString("type");
				if (type != null)
				{
					int level = configEnchantment.getInt("level", 1);
					double probability = configEnchantment.getDouble("probability", 1.0);
					ProbabilisticEnchantment enchantment = new ProbabilisticEnchantment(name, type, level, probability);
					enchantments.add(enchantment);
				}
			}
		}
		return enchantments;
	}
	
	private List<PotionEffect> getPotionEffects(ConfigurationSection configurationSection) {
		List<PotionEffect> potionEffects = Lists.newArrayList();
		if(configurationSection != null)
		{
			for (String name : configurationSection.getKeys(false))
			{
				ConfigurationSection configEffect = configurationSection.getConfigurationSection(name);
				String type = configEffect.getString("type");
				if (type != null)
				{
					PotionEffectType effect = PotionEffectType.getByName(type);
					if (effect != null) {
						int duration = configEffect.getInt("duration", 200);
						int amplifier = configEffect.getInt("amplifier", 0);
						potionEffects.add(new PotionEffect(effect, duration, amplifier));
					}
				}
			}
		}
		return potionEffects;
	}
	
	public ItemList<NamedItemStack> getItems(ConfigurationSection configItems)
	{
		ItemList<NamedItemStack> items = new ItemList<NamedItemStack>();
		if(configItems != null)
		{
			for(String commonName : configItems.getKeys(false))
			{
				
				ConfigurationSection configItem = configItems.getConfigurationSection(commonName);
				String materialName = configItem.getString("material");
				Material material = Material.getMaterial(materialName);
				//only proceeds if an acceptable material name was provided
				if (material == null)
				{
					getLogger().severe(configItems.getCurrentPath() + " requires invalid material " + materialName);
				}
				else
				{
					int amount = configItem.getInt("amount", 1);
					short durability = (short)configItem.getInt("durability", 0);
					int repairCost = (short)configItem.getInt("repair_cost", 0);
					String displayName = configItem.getString("display_name");
					String lore = configItem.getString("lore");
					List<ProbabilisticEnchantment> compulsoryEnchantments = getEnchantments(configItem.getConfigurationSection("enchantments"));
					List<ProbabilisticEnchantment> storedEnchantments = getEnchantments(configItem.getConfigurationSection("stored_enchantments"));
					List<PotionEffect> potionEffects = getPotionEffects(configItem.getConfigurationSection("potion_effects"));
					items.add(createItemStack(material, amount, durability, displayName, lore, commonName, repairCost, compulsoryEnchantments, storedEnchantments, potionEffects));
				}
			}
		}
		
		return items;
	}

	private NamedItemStack createItemStack(Material material, int stackSize, short durability, String name, String loreString, String commonName, int repairCost, 
			List<ProbabilisticEnchantment> compulsoryEnchants, List<ProbabilisticEnchantment> storedEnchants, List<PotionEffect> potionEffects)
	{
		NamedItemStack namedItemStack= new NamedItemStack(material, stackSize, durability,commonName);
		if(name != null || loreString != null || compulsoryEnchants.size() > 0 || storedEnchants.size() > 0 || potionEffects.size() > 0 || repairCost > 0)
		{
			ItemMeta meta = namedItemStack.getItemMeta();
			
			if (name != null) {
				meta.setDisplayName(name);
			}
			
			if (meta instanceof Repairable && repairCost > 0) {
				((Repairable) meta).setRepairCost(repairCost);
			}
			
			if (loreString!=null) {
				List<String> lore = Lists.newArrayList();
				lore.add(loreString);
				meta.setLore(lore);
			}
			
			for (ProbabilisticEnchantment enchant : compulsoryEnchants) {
				meta.addEnchant(enchant.getEnchantment(), enchant.getLevel(), false);
			}
			
			if (meta instanceof EnchantmentStorageMeta) {
				EnchantmentStorageMeta esm = (EnchantmentStorageMeta) meta;
				for (ProbabilisticEnchantment enchant : storedEnchants) {
					esm.addStoredEnchant(enchant.getEnchantment(), enchant.getLevel(), false);
				}
			}
			
			if (meta instanceof PotionMeta) {
				PotionMeta pm = (PotionMeta) meta;
				for (PotionEffect effect : potionEffects) {
					pm.addCustomEffect(effect, true);
				}
			}
			
			namedItemStack.setItemMeta(meta);
		}
		
		return namedItemStack;
	}
	
	public static IFactoryProperties getProperties(FactoryType factoryType, String subFactoryType)
	{
		switch(factoryType)
		{
			case PRODUCTION:
				return FactoryModPlugin.productionProperties.get(subFactoryType);
			default:
				return null;
		}
	}

	public static int getMaxTiers(FactoryType factoryType) 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public static void sendConsoleMessage(String message) 
	{
		Bukkit.getLogger().info(FactoryModPlugin.PLUGIN_PREFIX + message);	
	}

	public static void logFileError(String fileName, int lineNum, String error) {
		sendConsoleMessage(new StringBuilder("ERROR at line ")
			.append(lineNum).append(" of ").append(fileName).append(": ").append(error).toString());
	}

	public PrintingPressProperties getPrintingPressProperties() {
		return printingPressProperties;
	}
	
	public NetherFactoryProperties getNetherFactoryProperties() {
		return netherFactoryProperties;
	}
	public RepairFactoryProperties getRepairFactoryProperties() {
		return repairFactoryProperties;
	}
	
	public CompactorProperties getCompactorProperties() {
	    return compactorProperties;
	}
	
	private static FactoryModPlugin plugin;
	public static FactoryModPlugin getPlugin(){
		return plugin;
	}
}
