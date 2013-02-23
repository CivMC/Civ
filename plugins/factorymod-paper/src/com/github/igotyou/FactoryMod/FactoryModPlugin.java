package com.github.igotyou.FactoryMod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.igotyou.FactoryMod.managers.FactoryModManager;

public class FactoryModPlugin extends JavaPlugin
{
	FactoryModManager manager;
	public static int AMOUNT_OF_RECIPES_TO_REMOVE;
	
	public void onEnable()
	{
		initConfig();
	}
	
	public void onDisable()
	{
		
	}
	
	public void initConfig()
	{
		AMOUNT_OF_RECIPES_TO_REMOVE = getConfig().getInt("disabled_recipes.amount");
		
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
}
