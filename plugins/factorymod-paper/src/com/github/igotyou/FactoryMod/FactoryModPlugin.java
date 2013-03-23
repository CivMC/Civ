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
		productionRecipes = new ArrayList<ProductionRecipe>();
		FileConfiguration config = getConfig();
	
		this.saveDefaultConfig();
		
		//how often should the managers save?
		SAVE_CYCLE = config.getInt("general.save_cycle");
		//how many vanila recipes should be removed?
		AMOUNT_OF_RECIPES_TO_REMOVE = config.getInt("disabled_recipes.amount");
		//what's the material of the center block of factorys?
		CENTRAL_BLOCK_MATERIAL = Material.getMaterial(config.getString("general.central_block"));
		//Return the build materials upon destruction of factory.
		RETURN_BUILD_MATERIALS = config.getBoolean("general.return_build_materials");
		//is citadel enabled?
		CITADEL_ENABLED = config.getBoolean("general.citadel_enabled");
		//what's the tool that we use to interact with the factorys?
		FACTORY_INTERACTION_MATERIAL = Material.getMaterial(config.getString("general.factory_interaction_material"));
		int g = 0;
		//loop trough all the vanilla recipes we want to disable
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
		//how many production recipes are there?
		AMOUNT_OF_PRODUCTION_RECIPES = config.getInt("production_recipes.amount");
		//Temporarily store the indexs of the outputRecipes until all recipes are initliazed
		ArrayList<ArrayList> outputRecipesNumbers=new ArrayList<ArrayList>(AMOUNT_OF_PRODUCTION_RECIPES);
		//loop trough all production recipes
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
			Byte output_data = 0;
			Material outputMaterial = Material.getMaterial(config.getString(getPathToProductionRecipe(i) + ".output_material"));
			try
			{
				//I don't fully understand this, but it appears that it should be ".output_data" not ".data"
				//possibly this never succeeds...
				output_data = (byte) config.getInt(getPathToProductionRecipe(i) + ".data");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ItemStack output = new ItemStack(outputMaterial);
			if (output_data != 0 && durability != 0)
			{
				output = new ItemStack(outputMaterial, 1, durability, output_data);
			}
			else if (output_data != 0 && durability == 0)
			{
				output = new ItemStack(outputMaterial, 1, (short) 0, output_data);
			}

			
			HashMap<Integer, ItemStack> input = new HashMap<Integer, ItemStack>();
			HashMap<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
						
			for (int i1 = 1; i1 <= config.getInt(getPathToProductionRecipe(i) + ".amount_of_material_inputs"); i1++)
			{
				Byte data = 0;
				try
				{
					data = (byte) config.getInt(getPathToProductionRecipe(i) + ".input_data_" + String.valueOf(i1));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				int amount = config.getInt(getPathToProductionRecipe(i) + ".input_amount_" + String.valueOf(i1));
				Material material = Material.getMaterial(config.getString(getPathToProductionRecipe(i) + ".input_material_" + String.valueOf(i1)));
				if (material == null && "NETHER_STALK".equals(config.getString(getPathToProductionRecipe(i) + ".input_material_" + String.valueOf(i1))))
				{
					material = Material.getMaterial(372);
				}
				if (amount > 64)
				{
					while(amount > 64)
					{
						if (data != 0)
						{
							ItemStack itemStack = new ItemStack(material, 64, (short) 0, data);
							input.put(input.size()+1, itemStack);
						}
						else
						{
							ItemStack itemStack = new ItemStack(material, 64);
							input.put(input.size()+1, itemStack);
						}
						amount = amount - 64;
					}
					if (data != 0)
					{
						ItemStack itemStack = new ItemStack(material, amount, (short) 0, data);
						input.put(input.size()+1, itemStack);
					}
					else
					{
						ItemStack itemStack = new ItemStack(material, amount);
						input.put(input.size()+1, itemStack);
					}
				}
				else
				{
					if (data != 0)
					{
						ItemStack itemStack = new ItemStack(material, amount, (short) 0, data);
						input.put(input.size()+1, itemStack);
					}
					else
					{
						ItemStack itemStack = new ItemStack(material, amount);
						input.put(input.size()+1, itemStack);
					}
				}
			}
			for (int i1 = 1; i1 <= config.getInt(getPathToProductionRecipe(i) + ".amount_of_enchantments"); i1++)
			{
				String enchantmentName = config.getString(getPathToProductionRecipe(i) + ".enchantment_" + String.valueOf(i1));
				int enchantmentLevel = config.getInt(getPathToProductionRecipe(i) + ".enchantment_" + String.valueOf(i1) + "_level");
				enchantments.put(Enchantment.getByName(enchantmentName), enchantmentLevel);
			}
			
			//Stores where recipes should point since some of the ProductionRecipe objects may not have been generated yet
			
			ArrayList <Integer> currentRecipesNumbers=new ArrayList<Integer>();
			for (int i1=1; i1 <= config.getInt(getPathToProductionRecipe(i) + ".amount_of_output_recipes");i1++)
			{
				currentRecipesNumbers.add(config.getInt(getPathToProductionRecipe(i) + ".output_recipe_" + String.valueOf(i1)));
			}
			outputRecipesNumbers.add(i-1,currentRecipesNumbers);
			
			boolean useOnce=false;
			try
			{
				//I don't fully understand this, but it appears that it should be ".output_data" not ".data"
				//possibly this never succeeds...
				useOnce = (boolean) config.getBoolean(getPathToProductionRecipe(i) + ".use_once");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			if(enchantments.size() != 0)
			{
				ProductionRecipe recipe = new ProductionRecipe(input, output, batchAmount, recipeName, productionTime, i, useOnce, enchantments);
				productionRecipes.add(recipe);
			}
			else
			{
				ProductionRecipe recipe = new ProductionRecipe(input, output, batchAmount, recipeName, productionTime, i, useOnce);
				productionRecipes.add(recipe);
			}
		}
		
		//Once ProductionRecipe objects have been created correctly insert different pointers
		
		for(int i=0; i<outputRecipesNumbers.size(); i++)
		{
			ArrayList <Integer> currentRecipeOutputNumbers=outputRecipesNumbers.get(i);
			for(int i1=0; i1<currentRecipeOutputNumbers.size(); i1++)
			{
				int outputRecipeIndex=((Integer)currentRecipeOutputNumbers.get(i1)).intValue()-1;
				productionRecipes.get(i).addOutputRecipe(productionRecipes.get(outputRecipeIndex));
			}
		}
		
		AMOUNT_OF_PRODUCTION_FACTORY_TYPES = config.getInt("production_general.amount_of_factory_types");
		PRODUCER_UPDATE_CYCLE = config.getInt("production_general.update_cycle");
		for (int i = 1; i <=FactoryModPlugin.AMOUNT_OF_PRODUCTION_FACTORY_TYPES; i++)
		{
			HashMap<Integer, ItemStack> buildMaterials= new HashMap<Integer, ItemStack>();
			List<ProductionRecipe> recipes = new ArrayList<ProductionRecipe>();
		
			String name = config.getString(getPathToFactory(i) + ".name");
			String subFactoryType = config.getString(getPathToFactory(i) + ".sub_factory_type");
			Material energyMaterial = Material.getMaterial(config.getString(getPathToFactory(i) + ".fuel_material"));
			int fuelTime = config.getInt(getPathToFactory(i) + ".fuel_time");
			int fuelConsumption = config.getInt(getPathToFactory(i) + ".fuel_consumption");
			Byte fuelData = 0;
			ItemStack fuelStack;
			try
			{
				fuelData = (byte) config.getInt(getPathToFactory(i) + ".fuel_data");
			}
			catch (Exception e)
			{
				
			}
			if (fuelData != 0)
			{
				fuelStack = new ItemStack(energyMaterial, fuelConsumption, (short) 0, fuelData);
			}
			else
			{
				fuelStack = new ItemStack(energyMaterial, fuelConsumption);
			}
			
			
			for (int i1 = 1; i1 <= config.getInt(getPathToFactory(i) + ".amount_of_build_materials"); i1++)
			{
				Byte data = 0;
				try
				{
					data = (byte) config.getInt(getPathToFactory(i) + ".build_data_" + String.valueOf(i1));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				int amount = config.getInt(getPathToFactory(i) + ".build_amount_" + String.valueOf(i1));
				Material material = Material.getMaterial(config.getString(getPathToFactory(i) + ".build_material_" + String.valueOf(i1)));
				if (material == null && "NETHER_STALK".equals(config.getString(getPathToFactory(i) + ".build_material_" + String.valueOf(i1))))
				{
					material = Material.getMaterial(372);
				}
				if (amount > 64)
				{
					while(amount > 64)
					{
						if (data != 0)
						{
							ItemStack itemStack = new ItemStack(material, 64, (short) 0, data);
							buildMaterials.put(buildMaterials.size()+1, itemStack);
						}
						else
						{
							ItemStack itemStack = new ItemStack(material, 64);
							buildMaterials.put(buildMaterials.size()+1, itemStack);
						}
						amount -= 64;
					}
					if (data != 0)
					{
						ItemStack itemStack = new ItemStack(material, amount, (short) 0, data);
						buildMaterials.put(buildMaterials.size()+1, itemStack);
					}
					else
					{
						ItemStack itemStack = new ItemStack(material, amount);
						buildMaterials.put(buildMaterials.size()+1, itemStack);
					}
				}
				else
				{
				if (data != 0)
					{
						ItemStack itemStack = new ItemStack(material, amount, (short) 0, data);
						buildMaterials.put(buildMaterials.size()+1, itemStack);
					}
					else
					{
						if (material == Material.NETHER_WARTS)
						{
							ItemStack itemStack = new ItemStack(material, amount, (short) 0,(byte) 0);
							buildMaterials.put(buildMaterials.size()+1, itemStack);
						}
						else
						{
							ItemStack itemStack = new ItemStack(material, amount);
							buildMaterials.put(buildMaterials.size()+1, itemStack);
						}
					}
				}
			}
			for(int i1 = 1; i1 <= config.getInt(getPathToFactory(i) + ".amount_of_production_recipes"); i1++)
			{
				recipes.add(productionRecipes.get(config.getInt(getPathToFactory(i) + ".recipe_" + String.valueOf(i1)) - 1));
			}
			ProductionProperties productionProperties = new ProductionProperties(buildMaterials, recipes, fuelStack, fuelTime, name);
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
