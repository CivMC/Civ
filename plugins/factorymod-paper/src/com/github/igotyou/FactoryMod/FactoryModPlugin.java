package com.github.igotyou.FactoryMod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.igotyou.FactoryMod.FactoryObject.FactoryType;
import com.github.igotyou.FactoryMod.interfaces.Properties;
import com.github.igotyou.FactoryMod.listeners.BlockListener;
import com.github.igotyou.FactoryMod.managers.FactoryModManager;
import com.github.igotyou.FactoryMod.managers.ProductionManager;
import com.github.igotyou.FactoryMod.properties.ProductionProperties;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;

public class FactoryModPlugin extends JavaPlugin
{

	FactoryModManager manager;
	public static HashMap<String, ProductionProperties> production_Properties;
	public static List<ProductionRecipe> productionRecipes;
	
	public static final String VERSION = "v1.0"; //Current version of plugin
	public static final String PLUGIN_NAME = "FactoryMod"; //Name of plugin
	public static final String PLUGIN_PREFIX = PLUGIN_NAME + " " + VERSION + ": ";
	public static final String PRODUCTION_SAVES_FILE = "productionSaves"; // The production saves file name
	public static final int TICKS_PER_SECOND = 20; //The number of ticks per second
	
	public static int AMOUNT_OF_RECIPES_TO_REMOVE;
	public static int PRODUCER_UPDATE_CYCLE;
	public static boolean PRODUCTION_ENEABLED;
	public static int SAVE_CYCLE;
	public static int AMOUNT_OF_PRODUCTION_RECIPES;
	public static int AMOUNT_OF_PRODUCTION_FACTORY_TYPES;
	public static Material CENTRAL_BLOCK_MATERIAL;
	public static boolean RETURN_BUILD_MATERIALS;
	public static boolean CITADEL_ENABLED;
	public static Material FACTORY_INTERACTION_MATERIAL;
	
	public void onEnable()
	{
		initConfig();
		manager = new FactoryModManager(this);
		registerEvents();
	}
	
	public void onDisable()
	{
		manager.onDisable();
	}
	
	public void registerEvents()
	{
		ProductionManager proMan;
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
		productionRecipes = new ArrayList<ProductionRecipe>();
		FileConfiguration config = getConfig();
		
		SAVE_CYCLE = config.getInt("general.save_cycle");
		AMOUNT_OF_RECIPES_TO_REMOVE = config.getInt("disabled_recipes.amount");
		CENTRAL_BLOCK_MATERIAL = Material.getMaterial(config.getString("general.central_block"));
		RETURN_BUILD_MATERIALS = config.getBoolean("general.return_build_materials");
		CITADEL_ENABLED = config.getBoolean("general.citadel_enabled");
		FACTORY_INTERACTION_MATERIAL = Material.getMaterial(config.getString("general.factory_interaction_material"));
		int g = 0;
		for (int i = 1; i <= FactoryModPlugin.AMOUNT_OF_RECIPES_TO_REMOVE; i++)
		{
	
			ItemStack recipeItemStack = new ItemStack(Material.getMaterial(config.getString(getPathToRecipe(i))));
			List<Recipe> tempList = getServer().getRecipesFor(recipeItemStack);
			for (int itterator = 0; itterator < tempList.size(); itterator ++)
			{
				removeRecipe(tempList.get(itterator));
				g++;
			}

		}
		sendConsoleMessage(g + " recipes removed");
		AMOUNT_OF_PRODUCTION_RECIPES = config.getInt("production_recipes.amount");
		for (int i =1; i <= FactoryModPlugin.AMOUNT_OF_PRODUCTION_RECIPES; i++)
		{
			String recipeName = config.getString(getPathToProductionRecipe(i) + ".name");
			int batchAmount = config.getInt(getPathToProductionRecipe(i)  + ".batch_amount");
			int productionTime = config.getInt(getPathToProductionRecipe(i)  + ".production_time");
			short durability = 0;
			if (config.getString(getPathToProductionRecipe(i) + ".durability") != "MAX")
			{
				durability = (short) config.getInt(getPathToProductionRecipe(i) + ".durability");
			}
			Material output = Material.getMaterial(config.getString(getPathToProductionRecipe(i) + ".output_material"));
			
			HashMap<Integer, Material> inputMaterials = new HashMap<Integer, Material>();
			HashMap<Integer, Integer> inputAmount = new HashMap<Integer, Integer>();
			HashMap<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
			
			for (int i1 = 1; i1 <= config.getInt(getPathToProductionRecipe(i) + ".amount_of_material_inputs"); i1++)
			{
				inputMaterials.put(i1, Material.getMaterial(config.getString(getPathToProductionRecipe(i) + ".input_material_" + String.valueOf(i1))));
				inputAmount.put(i1, config.getInt(getPathToProductionRecipe(i) + ".input_amount_" + String.valueOf(i1)));
			}
			for (int i1 = 1; i1 <= config.getInt(getPathToProductionRecipe(i) + ".amount_of_enchantments"); i1++)
			{
				String enchantmentName = config.getString(getPathToProductionRecipe(i) + ".enchantment_" + String.valueOf(i1));
				int enchantmentLevel = config.getInt(getPathToProductionRecipe(i) + ".enchantment_" + String.valueOf(i1) + "_level");
				enchantments.put(Enchantment.getByName(enchantmentName), enchantmentLevel);
			}
			
			if(durability != 0 && enchantments.size() > 0)
			{
				ProductionRecipe recipe = new ProductionRecipe(inputMaterials, inputAmount, output, batchAmount, recipeName, productionTime,enchantments,durability);
				productionRecipes.add(recipe);
			}
			else if (durability != 0 && enchantments.size() == 0)
			{
				ProductionRecipe recipe = new ProductionRecipe(inputMaterials, inputAmount, output, batchAmount, recipeName, productionTime,durability);
				productionRecipes.add(recipe);
			}
			else if (enchantments.size() != 0 && durability == 0)
			{
				ProductionRecipe recipe = new ProductionRecipe(inputMaterials, inputAmount, output, batchAmount, recipeName, productionTime,enchantments);
				productionRecipes.add(recipe);
			}
			else
			{
				ProductionRecipe recipe = new ProductionRecipe(inputMaterials, inputAmount, output, batchAmount, recipeName, productionTime);
				productionRecipes.add(recipe);
			}
		}
		
		AMOUNT_OF_PRODUCTION_FACTORY_TYPES = config.getInt("production_general.amount_of_factory_types");
		PRODUCER_UPDATE_CYCLE = config.getInt("production_general.update_cycle");
		for (int i = 1; i <=FactoryModPlugin.AMOUNT_OF_PRODUCTION_FACTORY_TYPES; i++)
		{
			HashMap<Integer, Material> buildMaterials= new HashMap<Integer, Material>();
			HashMap<Integer, Integer> buildAmount = new HashMap<Integer, Integer>();
			List<ProductionRecipe> recipes = new ArrayList<ProductionRecipe>();
		
			String name = config.getString(getPathToFactory(i) + ".name");
			String subFactoryType = config.getString(getPathToFactory(i) + ".sub_factory_type");
			Material energyMaterial = Material.getMaterial(config.getString(getPathToFactory(i) + ".fuel_material"));
			int fuelTime = config.getInt(getPathToFactory(i) + ".fuel_time");
			int fuelConsumption = config.getInt(getPathToFactory(i) + ".fuel_consumption");
			
			for (int i1 = 1; i1 <= config.getInt(getPathToFactory(i) + ".amount_of_build_materials"); i1++)
			{

				buildMaterials.put(i1, Material.getMaterial(config.getString(getPathToFactory(i) + ".build_material_" + String.valueOf(i1))));
				buildAmount.put(i1, config.getInt(getPathToFactory(i) + ".build_amount_" + String.valueOf(i1)));
			}
			for(int i1 = 1; i1 <= config.getInt(getPathToFactory(i) + ".amount_of_production_recipes"); i1++)
			{
				recipes.add(productionRecipes.get(config.getInt(getPathToFactory(i) + ".recipe_" + String.valueOf(i1)) - 1));
			}
			ProductionProperties productionProperties = new ProductionProperties(buildMaterials, buildAmount, recipes, energyMaterial, fuelTime, fuelConsumption, name);
			production_Properties.put(subFactoryType, productionProperties);
		}
	}
	
	private String getPathToRecipe(int i)
	{
		return "disabled_recipes.recipe_" + String.valueOf(i);
	}
	
	private String getPathToProductionRecipe(int i)
	{
		return "production_recipes.recipe_" + String.valueOf(i);
	}
	
	private String getPathToFactory(int i)
	{
		return "production_general.factory_" + String.valueOf(i);
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
