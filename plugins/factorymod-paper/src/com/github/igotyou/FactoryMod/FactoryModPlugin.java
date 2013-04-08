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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;

import com.github.igotyou.FactoryMod.FactoryObject.FactoryType;
import com.github.igotyou.FactoryMod.interfaces.Properties;
import com.github.igotyou.FactoryMod.listeners.BlockListener;
import com.github.igotyou.FactoryMod.managers.FactoryModManager;
import com.github.igotyou.FactoryMod.properties.ProductionProperties;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import com.github.igotyou.FactoryMod.recipes.ProbabilisticEnchantment;
import java.util.Map;


public class FactoryModPlugin extends JavaPlugin
{

	FactoryModManager manager;
	public static HashMap<String, ProductionProperties> production_Properties;
	public static HashMap<String,ProductionRecipe> productionRecipes;
	
	public static final String VERSION = "v1.0"; //Current version of plugin
	public static final String PLUGIN_NAME = "FactoryMod"; //Name of plugin
	public static final String PLUGIN_PREFIX = PLUGIN_NAME + " " + VERSION + ": ";
	public static final String PRODUCTION_SAVES_FILE = "productionSaves"; // The production saves file name
	public static final int TICKS_PER_SECOND = 20; //The number of ticks per second
	
	public static int PRODUCER_UPDATE_CYCLE;
	public static boolean PRODUCTION_ENEABLED;
	public static int SAVE_CYCLE;
	public static int AMOUNT_OF_PRODUCTION_RECIPES;
	public static int AMOUNT_OF_PRODUCTION_FACTORY_TYPES;
	public static Material CENTRAL_BLOCK_MATERIAL;
	public static boolean RETURN_BUILD_MATERIALS;
	public static boolean CITADEL_ENABLED;
	public static Material FACTORY_INTERACTION_MATERIAL;
	public static boolean DESTRUCTIBLE_FACTORIES;
	public static int MAINTENANCE_CYCLE;
	public static int MAINTENANCE_PERIOD;
	
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
			getServer().getPluginManager().registerEvents(new BlockListener(manager, manager.getProductionManager()), this);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void initConfig()
	{
		production_Properties = new HashMap<String, ProductionProperties>();
		productionRecipes = new HashMap<String,ProductionRecipe>();
		FileConfiguration config = getConfig();
		
		this.saveDefaultConfig();
		this.reloadConfig();
		config = getConfig();
		//how often should the managers save?
		SAVE_CYCLE = config.getInt("general.save_cycle");
		//what's the material of the center block of factorys?
		CENTRAL_BLOCK_MATERIAL = Material.getMaterial(config.getString("general.central_block"));
		//Return the build materials upon destruction of factory.
		RETURN_BUILD_MATERIALS = config.getBoolean("general.return_build_materials");
		//is citadel enabled?
		CITADEL_ENABLED = config.getBoolean("general.citadel_enabled");
		//what's the tool that we use to interact with the factorys?
		FACTORY_INTERACTION_MATERIAL = Material.getMaterial(config.getString("general.factory_interaction_material"));
		//If factories are removed upon destruction of their blocks
		DESTRUCTIBLE_FACTORIES=config.getBoolean("general.destructible_factories");		
		//How frequently factories are updated
		PRODUCER_UPDATE_CYCLE = config.getInt("production_general.update_cycle");
		//How frequently maintenance is update
		MAINTENANCE_CYCLE = config.getInt("production_general.maintenance_cycle");
		//How long it takes for factories to break down, modifiers upkeep costs
		MAINTENANCE_PERIOD = config.getInt("production_general.maintenance_period");
		//loop trough all the vanilla recipes we want to disable
		int g = 0;
		Iterator<String> disabledRecipes=config.getStringList("disabled_recipes").iterator();
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
		sendConsoleMessage(g + " recipes removed");

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
			int productionTime=configSection.getInt("production_time",1);
			//Inputs of the recipe, empty of there are no inputs
			Map<ItemStack,String> inputs = getItems(configSection.getConfigurationSection("inputs"));
			//Inputs of the recipe, empty of there are no inputs
			Map<ItemStack,String> upgrades = getItems(configSection.getConfigurationSection("upgrades"));
			//Outputs of the recipe, empty of there are no inputs
			Map<ItemStack,String> outputs = getItems(configSection.getConfigurationSection("outputs"));
			//Enchantments of the recipe, empty of there are no inputs
			List<ProbabilisticEnchantment> enchantments=getEnchantments(configSection.getConfigurationSection("enchantments"));
			//Whether this recipe can only be used once
			boolean useOnce = configSection.getBoolean("use_once");
			int maintenance = configSection.getInt("maintenance");
			ProductionRecipe recipe = new ProductionRecipe(title,recipeName,productionTime,inputs,upgrades,outputs,enchantments,useOnce,maintenance,new HashMap<ItemStack,String>());
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
			ItemStack fuel;
			Map<ItemStack,String> fuelStack=getItems(configSection.getConfigurationSection("fuel"));
			if(fuelStack.size()==0)
			{
				fuel=new ItemStack(Material.getMaterial("COAL"),1,(short)0,(byte) 1);
			}
			else
			{
				fuel=fuelStack.keySet().iterator().next();
			}
			int fuelTime=configSection.getInt("fuel_time",1);
			Map<ItemStack,String> buildMaterials=getItems(configSection.getConfigurationSection("inputs"));
			Map<ItemStack,String> repairs=getItems(configSection.getConfigurationSection("maintenance_input"));
			List<ProductionRecipe> factoryRecipes=new ArrayList<ProductionRecipe>();
			Iterator<String> ouputRecipeIterator=configSection.getStringList("production_recipes").iterator();
			while (ouputRecipeIterator.hasNext())
			{
				factoryRecipes.add(productionRecipes.get(ouputRecipeIterator.next()));
			}
			//Create repair recipe
			productionRecipes.put(title+"REPAIR",new ProductionRecipe(title+"REPAIR","Repair Factory",1,repairs));
			factoryRecipes.add(productionRecipes.get(title+"REPAIR"));
			ProductionProperties productionProperties = new ProductionProperties(buildMaterials, factoryRecipes, fuel, fuelTime, factoryName);
			production_Properties.put(title, productionProperties);
		}
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
	private Map<ItemStack,String> getItems(ConfigurationSection configItems)
	{
		Map<ItemStack,String> items=new HashMap<ItemStack,String>();
		if(configItems!=null)
		{
			Iterator<String> itemNames=configItems.getKeys(false).iterator();
			while(itemNames.hasNext())
			{
				
				String commonName=itemNames.next();
				ConfigurationSection configItem= configItems.getConfigurationSection(commonName);
				String materialName=configItem.getString("material");
				Material material = Material.getMaterial(materialName);
				//only proceeds if an acceptable material name was provided
				if(material!=null)
				{
					int amount=configItem.getInt("amount",1);
					short damage=(short)configItem.getInt("durability",0);
					String displayName=configItem.getString("display_name");
					String lore=configItem.getString("lore");
					if (material == null && "NETHER_STALK".equals(materialName))
						{
							material = Material.getMaterial(372);
						}
					int stackSize=material.getMaxStackSize();
					while(amount>stackSize)
					{
						ItemStack itemStack =createItemStack(material,stackSize,damage,displayName,lore);
						items.put(itemStack,commonName);					
						amount = amount - stackSize;	
					}
					ItemStack itemStack =createItemStack(material,amount,damage,displayName,lore);
					items.put(itemStack,commonName);
				}
			}
		}
		return items;
	}
	
	private ItemStack createItemStack(Material material,int stackSize,short durability,String name,String loreString)
	{
		ItemStack itemStack;
		itemStack= new ItemStack(material, stackSize, durability);
		ItemMeta meta=itemStack.getItemMeta();
		if (name!=null)
			meta.setDisplayName(name);
		if (loreString!=null)
		{
			List<String> lore = new ArrayList<String>();
			lore.add(loreString);
			meta.setLore(lore);
		}
		itemStack.setItemMeta(meta);
		return itemStack;
			
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
				return FactoryModPlugin.production_Properties.get(subFactoryType);
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
}
