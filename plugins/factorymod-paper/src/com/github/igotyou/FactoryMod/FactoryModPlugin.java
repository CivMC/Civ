package com.github.igotyou.FactoryMod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.igotyou.FactoryMod.FactoryObject.FactoryType;
import com.github.igotyou.FactoryMod.FactoryObject.SubFactoryType;
import com.github.igotyou.FactoryMod.interfaces.Properties;
import com.github.igotyou.FactoryMod.managers.FactoryModManager;
import com.github.igotyou.FactoryMod.properties.ProductionProperties;

public class FactoryModPlugin extends JavaPlugin
{

	FactoryModManager manager;
	public static HashMap<SubFactoryType, ProductionProperties> Production_Properties;
	
	public static final String VERSION = "v1.0"; //Current version of plugin
	public static final String PLUGIN_NAME = "FactoryMod"; //Name of plugin
	public static final String PLUGIN_PREFIX = PLUGIN_NAME + " " + VERSION + ": ";
	public static final String PRODUCTION_SAVES_FILE = "productionSaves"; // The ore gin saves file name
	public static final int TICKS_PER_SECOND = 20; //The number of ticks per second
	
	public static int AMOUNT_OF_RECIPES_TO_REMOVE;
	public static int PRODUCTION_MAX_TIERS;
	public static int PRODUCER_UPDATE_CYCLE;
	public static boolean PRODUCTION_ENEABLED;
	public static int SAVE_CYCLE;
	
	public void onEnable()
	{
		initConfig();
	}
	
	public void onDisable()
	{
		
	}
	
	public void initConfig()
	{
		Production_Properties = new HashMap<SubFactoryType, ProductionProperties>();
		AMOUNT_OF_RECIPES_TO_REMOVE = getConfig().getInt("disabled_recipes.amount");
		PRODUCTION_MAX_TIERS = getConfig().getInt("production_general.max_tiers");
		
		for (int i = 1; i <= FactoryModPlugin.AMOUNT_OF_RECIPES_TO_REMOVE; i++)
		{
			int g = 0;
			ItemStack recipeItemStack = new ItemStack(Material.getMaterial(getConfig().getString(getPathToRecipe(i))));
			List<Recipe> tempList = getServer().getRecipesFor(recipeItemStack);
			for (int itterator = 0; itterator < tempList.size(); itterator ++)
			{
				removeRecipe(tempList.get(itterator));
				g++;
			}
			getLogger().info(g + " recipes removed");
		}
		for (int i = 1; i <= FactoryModPlugin.PRODUCTION_MAX_TIERS; i++)
		{
			int amount_of_materials = getConfig().getInt("production_general.amount_of_materials");
		}
	}
	
	private String getPathToRecipe(int i)
	{
		return "disabled_recipes.recipe_" + String.valueOf(i);
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

	public static Properties getProperties(FactoryType factoryType, SubFactoryType subFactoryType)
	{
		switch(factoryType)
		{
			case PRODUCTION:
				return FactoryModPlugin.Production_Properties.get(subFactoryType);
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
