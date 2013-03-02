package com.github.igotyou.FactoryMod.recipes;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.interfaces.Recipe;

public class ProductionRecipe implements Recipe
{
	private HashMap <Integer, ItemStack> input;
	private ItemStack output;
	private HashMap <Enchantment, Integer> enchantments;
	private int batchAmount;
	private int productionTime;
	private String recipeName;
	
	public ProductionRecipe(HashMap<Integer, ItemStack> input, ItemStack output,
			int batchAmount, String recipeName, int productionTime)
	{
		this.input = input;
		this.output = output;
		this.batchAmount = batchAmount;
		this.recipeName = recipeName;
		this.productionTime = productionTime;
	}
	
	public ProductionRecipe(HashMap<Integer, ItemStack> input, ItemStack output, int batchAmount, String recipeName, 
			int productionTime, HashMap <Enchantment, Integer> enchantments)
	{
		this.input = input;
		this.output = output;
		this.batchAmount = batchAmount;
		this.recipeName = recipeName;
		this.productionTime = productionTime;
		this.enchantments = enchantments;
	}
	
	public int getBatchAmount() 
	{
		return batchAmount;
	}

	public ItemStack getOutput() 
	{
		return output;
	}

	public HashMap<Integer, ItemStack> getInput()
	{
		return input;
	}
	
	public HashMap<Enchantment, Integer> getEnchantments()
	{
		return enchantments;
	}
	
	public String getRecipeName() 
	{
		return recipeName;
	}

	public int getProductionTime() 
	{
		return productionTime;
	}



}
