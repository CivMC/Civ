package com.github.igotyou.FactoryMod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.configuration.ConfigurationSection;

import com.github.igotyou.FactoryMod.FactoryObject.FactoryType;
import com.github.igotyou.FactoryMod.interfaces.Properties;
import com.github.igotyou.FactoryMod.listeners.FactoryModListener;
import com.github.igotyou.FactoryMod.listeners.NoteStackListener;
import com.github.igotyou.FactoryMod.listeners.RedstoneListener;
import com.github.igotyou.FactoryMod.managers.FactoryModManager;
import com.github.igotyou.FactoryMod.properties.NetherFactoryProperties;
import com.github.igotyou.FactoryMod.properties.PrintingPressProperties;
import com.github.igotyou.FactoryMod.properties.ProductionProperties;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import com.github.igotyou.FactoryMod.recipes.ProbabilisticEnchantment;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;

import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;


public class FactoryModPlugin extends JavaPlugin
{

	FactoryModManager manager;
	public static HashMap<String, ProductionProperties> productionProperties;
	public static HashMap<String,ProductionRecipe> productionRecipes;
	public PrintingPressProperties printingPressProperties;
	public NetherFactoryProperties netherFactoryProperties;
	
	public static final String VERSION = "v1.0"; //Current version of plugin
	public static final String PLUGIN_NAME = "FactoryMod"; //Name of plugin
	public static final String PLUGIN_PREFIX = PLUGIN_NAME + " " + VERSION + ": ";
	public static final String PRODUCTION_SAVES_FILE = "productionSaves"; // The production saves file name
	public static final int TICKS_PER_SECOND = 20; //The number of ticks per second
	public static final String PRINTING_PRESSES_SAVE_FILE = "pressSaves";
	
	public static final String NETHER_FACTORY_SAVE_FILE = "netherSaves";
	public static boolean DISABLE_PORTALS;
	public static int NETHER_SCALE;
	public static boolean ALLOW_REINFORCEMENT_CREATION_ABOVE_TELEPORT_PLATFORM;
	public static boolean ALLOW_BLOCK_PLACEMENT_ABOVE_TELEPORT_PLATFORM;
	public static boolean TELEPORT_PLATFORM_INVUNERABLE;
	public static boolean REGENERATE_TELEPORT_BLOCK_ON_TELEPORT;
	public static boolean REMOVE_BLOCK_ABOVE_TELEPORT_PLATFORM_ON_TELEPORT;
	public static String WORLD_NAME;
	public static String NETHER_NAME;
	
	public static int PRODUCER_UPDATE_CYCLE;
	public static boolean PRODUCTION_ENEABLED;
	public static int SAVE_CYCLE;
	public static Material CENTRAL_BLOCK_MATERIAL;
	public static boolean RETURN_BUILD_MATERIALS;
	public static boolean CITADEL_ENABLED;
	public static Material FACTORY_INTERACTION_MATERIAL;
	public static boolean DESTRUCTIBLE_FACTORIES;
	public static int NETHER_MARKER_MAX_DISTANCE;
	public static Material NETHER_FACTORY_TELEPORT_PLATFORM_MATERIAL;
	public static Material NETHER_FACTORY_MARKER_MATERIAL;
	public static boolean DISABLE_EXPERIENCE;
	public static long DISREPAIR_PERIOD;
	public static long REPAIR_PERIOD;
	public static boolean REDSTONE_START_ENABLED;
	public static boolean LEVER_OUTPUT_ENABLED;
	
	public void onEnable()
	{
		//load the config.yml
		initConfig();
		//create the main manager
		manager = new FactoryModManager(this);
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
			getServer().getPluginManager().registerEvents(new RedstoneListener(manager, manager.getProductionManager()), this);
			getServer().getPluginManager().registerEvents(new NoteStackListener(this), this);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void initConfig()
	{
		productionProperties = new HashMap<String, ProductionProperties>();
		productionRecipes = new HashMap<String,ProductionRecipe>();
		FileConfiguration config = getConfig();
		if(getConfig().getDefaults().getBoolean("copy_defaults", true))
		{
			saveResource("config.yml",true);
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
		int g = 0;
		Iterator<String> disabledRecipes=config.getStringList("crafting.disable").iterator();
		while(disabledRecipes.hasNext())
		{
			ItemStack recipeItemStack = new ItemStack(Material.getMaterial(disabledRecipes.next()));
			List<Recipe> tempList = getServer().getRecipesFor(recipeItemStack);
			for (int itterator = 0; itterator < tempList.size(); itterator ++)
			{
				removeRecipe(tempList.get(itterator));
				g++;
			}

		}
		//Enable the following recipes
		ConfigurationSection configCraftingEnable=config.getConfigurationSection("crafting.enable");
		for (String recipeName:configCraftingEnable.getKeys(false))
		{
			ConfigurationSection configSection=configCraftingEnable.getConfigurationSection(recipeName);
			Recipe recipe;
			List<String> shape=configSection.getStringList("shape");
			NamedItemStack output=getItems(configSection.getConfigurationSection("output")).get(0);
			if(shape.isEmpty())
			{
				ShapelessRecipe shapelessRecipe=new ShapelessRecipe(output);
				for (ItemStack input:getItems(configSection.getConfigurationSection("inputs")))
				{
					shapelessRecipe.addIngredient(input.getAmount(),input.getType(),input.getDurability());
				}
				recipe=shapelessRecipe;
			}
			else
			{
				ShapedRecipe shapedRecipe=new ShapedRecipe(output);
				shapedRecipe.shape(shape.toArray(new String[shape.size()]));
				for(String inputKey:configSection.getConfigurationSection("inputs").getKeys(false))
				{
					ItemStack input=getItems(configSection.getConfigurationSection("inputs."+inputKey)).get(0);
					shapedRecipe.setIngredient(inputKey.charAt(0),input.getType(),input.getDurability());
				}
				recipe=shapedRecipe;
			}
			Bukkit.addRecipe(recipe);
		}
		
		//Import recipes from config.yml
		ConfigurationSection configProdRecipes=config.getConfigurationSection("production_recipes");
		//Temporary Storage array to store where recipes should point to each other
		HashMap<ProductionRecipe,ArrayList> outputRecipes=new HashMap<ProductionRecipe,ArrayList>();
		Iterator<String> recipeTitles=configProdRecipes.getKeys(false).iterator();
		while (recipeTitles.hasNext())
		{
			//Section header in recipe file, also serves as unique identifier for the recipe
			//All spaces are replaced with udnerscores so they don't disrupt saving format
			//There should be a check for uniqueness of this identifier...
			String title=recipeTitles.next();
			ConfigurationSection configSection=configProdRecipes.getConfigurationSection(title);
			title=title.replaceAll(" ","_");
			//Display name of the recipe, Deafult of "Default Name"
			String recipeName = configSection.getString("name","Default Name");
			//Production time of the recipe, default of 1
			int productionTime=configSection.getInt("production_time",2);
			//Inputs of the recipe, empty of there are no inputs
			ItemList<NamedItemStack> inputs = getItems(configSection.getConfigurationSection("inputs"));
			//Inputs of the recipe, empty of there are no inputs
			ItemList<NamedItemStack> upgrades = getItems(configSection.getConfigurationSection("upgrades"));
			//Outputs of the recipe, empty of there are no inputs
			ItemList<NamedItemStack> outputs = getItems(configSection.getConfigurationSection("outputs"));
			//Enchantments of the recipe, empty of there are no inputs
			List<ProbabilisticEnchantment> enchantments=getEnchantments(configSection.getConfigurationSection("enchantments"));
			//Whether this recipe can only be used once
			boolean useOnce = configSection.getBoolean("use_once");
			ProductionRecipe recipe = new ProductionRecipe(title,recipeName,productionTime,inputs,upgrades,outputs,enchantments,useOnce,new ItemList<NamedItemStack>());
			productionRecipes.put(title,recipe);
			//Store the titles of the recipes that this should point to
			ArrayList <String> currentOutputRecipes=new ArrayList<String>();
			currentOutputRecipes.addAll(configSection.getStringList("output_recipes"));
			outputRecipes.put(recipe,currentOutputRecipes);
		}
		//Once ProductionRecipe objects have been created correctly insert different pointers
		Iterator<ProductionRecipe> recipeIterator=outputRecipes.keySet().iterator();
		while (recipeIterator.hasNext())
		{
			ProductionRecipe recipe=recipeIterator.next();
			Iterator<String> outputIterator=outputRecipes.get(recipe).iterator();
			while(outputIterator.hasNext())
			{
				recipe.addOutputRecipe(productionRecipes.get(outputIterator.next()));
			}
		}
		
		
		//Import factories from config.yml
		ConfigurationSection configProdFactories=config.getConfigurationSection("production_factories");
		Iterator<String> factoryTitles=configProdFactories.getKeys(false).iterator();
		while(factoryTitles.hasNext())
		{
			String title=factoryTitles.next();
			ConfigurationSection configSection=configProdFactories.getConfigurationSection(title);
			title=title.replaceAll(" ","_");
			String factoryName=configSection.getString("name","Default Name");
			//Uses overpowered getItems method for consistency, should always return a list of size=1
			//If no fuel is found, default to charcoal
			ItemList<NamedItemStack> fuel=getItems(configSection.getConfigurationSection("fuel"));
			if(fuel.isEmpty())
			{
				fuel=new ItemList<NamedItemStack>();
				fuel.add(new NamedItemStack(Material.getMaterial("COAL"),1,(short)1,"Charcoal"));
			}
			int fuelTime=configSection.getInt("fuel_time",2);
			ItemList<NamedItemStack> inputs=getItems(configSection.getConfigurationSection("inputs"));
			ItemList<NamedItemStack> repairs=getItems(configSection.getConfigurationSection("repair_inputs"));
			List<ProductionRecipe> factoryRecipes=new ArrayList<ProductionRecipe>();
			Iterator<String> ouputRecipeIterator=configSection.getStringList("recipes").iterator();
			while (ouputRecipeIterator.hasNext())
			{
				factoryRecipes.add(productionRecipes.get(ouputRecipeIterator.next()));
			}
			int repair=configSection.getInt("repair_multiple",0);
			//Create repair recipe
			productionRecipes.put(title+"REPAIR",new ProductionRecipe(title+"REPAIR","Repair Factory",1,repairs));
			factoryRecipes.add(productionRecipes.get(title+"REPAIR"));
			ProductionProperties productionProperty = new ProductionProperties(inputs, factoryRecipes, fuel, fuelTime, factoryName, repair);
			productionProperties.put(title, productionProperty);
		}
		
		ConfigurationSection configPrintingPresses=config.getConfigurationSection("printing_presses");
		ConfigurationSection configNetherFactory=config.getConfigurationSection("nether_factory");
		printingPressProperties = PrintingPressProperties.fromConfig(this, configPrintingPresses);
		netherFactoryProperties = NetherFactoryProperties.fromConfig(this, configNetherFactory);
	}
	
	private List<ProbabilisticEnchantment> getEnchantments(ConfigurationSection configEnchantments)
	{
		List<ProbabilisticEnchantment> enchantments=new ArrayList<ProbabilisticEnchantment>();
		if(configEnchantments!=null)
		{
			Iterator<String> names=configEnchantments.getKeys(false).iterator();
			while (names.hasNext())
			{
				String name=names.next();
				ConfigurationSection configEnchantment=configEnchantments.getConfigurationSection(name);
				String type=configEnchantment.getString("type");
				if (type!=null)
				{
					int level=configEnchantment.getInt("level",1);
					double probability=configEnchantment.getDouble("probability",1.0);
					ProbabilisticEnchantment enchantment=new ProbabilisticEnchantment(name,type,level,probability);
					enchantments.add(enchantment);
				}
			}
		}
		return enchantments;
	}
	
	private List<PotionEffect> getPotionEffects(
			ConfigurationSection configurationSection) {
		List<PotionEffect> potionEffects = new ArrayList<PotionEffect>();
		if(configurationSection!=null)
		{
			Iterator<String> names=configurationSection.getKeys(false).iterator();
			while (names.hasNext())
			{
				String name=names.next();
				ConfigurationSection configEffect=configurationSection.getConfigurationSection(name);
				String type=configEffect.getString("type");
				if (type!=null)
				{
					PotionEffectType effect = PotionEffectType.getByName(type);
					if (effect != null) {
						int duration=configEffect.getInt("duration",200);
						int amplifier=configEffect.getInt("amplifier",0);
						potionEffects.add(new PotionEffect(effect, duration, amplifier));
					}
				}
			}
		}
		return potionEffects;
	}
	
	public ItemList<NamedItemStack> getItems(ConfigurationSection configItems)
	{
		ItemList<NamedItemStack> items=new ItemList<NamedItemStack>();
		if(configItems!=null)
		{
			for(String commonName:configItems.getKeys(false))
			{
				
				ConfigurationSection configItem= configItems.getConfigurationSection(commonName);
				String materialName=configItem.getString("material");
				Material material = Material.getMaterial(materialName);
				//only proceeds if an acceptable material name was provided
				if (material == null)
				{
					getLogger().severe(configItems.getCurrentPath() + " requires invalid material " + materialName);
				}
				else
				{
					int amount=configItem.getInt("amount",1);
					short durability=(short)configItem.getInt("durability",0);
					int repairCost=(short)configItem.getInt("repair_cost",0);
					String displayName=configItem.getString("display_name");
					String lore=configItem.getString("lore");
					List<ProbabilisticEnchantment> compulsoryEnchantments = getEnchantments(configItem.getConfigurationSection("enchantments"));
					List<ProbabilisticEnchantment> storedEnchantments = getEnchantments(configItem.getConfigurationSection("stored_enchantments"));
					List<PotionEffect> potionEffects = getPotionEffects(configItem.getConfigurationSection("potion_effects"));
					items.add(createItemStack(material,amount,durability,displayName,lore,commonName,repairCost,compulsoryEnchantments,storedEnchantments,potionEffects));
				}
			}
		}
		return items;
	}

	private NamedItemStack createItemStack(Material material,int stackSize,short durability,String name,String loreString,String commonName,int repairCost,List<ProbabilisticEnchantment> compulsoryEnchants,List<ProbabilisticEnchantment> storedEnchants, List<PotionEffect> potionEffects)
	{
		NamedItemStack namedItemStack= new NamedItemStack(material, stackSize, durability,commonName);
		if(name!=null||loreString!=null||compulsoryEnchants.size()>0||storedEnchants.size()>0||potionEffects.size()>0||repairCost > 0)
		{
			ItemMeta meta=namedItemStack.getItemMeta();
			if (name!=null)
				meta.setDisplayName(name);
			if (meta instanceof Repairable && repairCost > 0)
				((Repairable) meta).setRepairCost(repairCost);
			if (loreString!=null)
			{
				List<String> lore = new ArrayList<String>();
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
	
	private void removeRecipe(Recipe removalRecipe)
	{
		Iterator<Recipe> itterator = getServer().recipeIterator();
		while (itterator.hasNext())
		{
			Recipe recipe = itterator.next();
			if (recipe.getResult().getType() == removalRecipe.getResult().getType())
			{
				itterator.remove();
			}
		}
	}

	public static Properties getProperties(FactoryType factoryType, String subFactoryType)
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

	public PrintingPressProperties getPrintingPressProperties() {
		return printingPressProperties;
	}
	
	public NetherFactoryProperties getNetherFactoryProperties() {
		return netherFactoryProperties;
	}
}
