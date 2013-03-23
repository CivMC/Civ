package com.github.igotyou.FactoryMod.recipes;

import java.util.HashMap;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.interfaces.Recipe;
import java.util.ArrayList;

public class ProductionRecipe implements Recipe
{
	private HashMap <Integer, ItemStack> input;
	private ItemStack output;
	private HashMap <Enchantment, Integer> enchantments;
	private int batchAmount;
	private int productionTime;
	private String recipeName;
	private int number;
	private ArrayList <ProductionRecipe> outputRecipes;
	private boolean useOnce;
	
	public ProductionRecipe(HashMap<Integer, ItemStack> input, ItemStack output,
			int batchAmount, String recipeName, int productionTime,int number,boolean useOnce)
	{
		this.input = input;
		this.output = output;
		this.batchAmount = batchAmount;
		this.recipeName = recipeName;
		this.productionTime = productionTime;
		this.number=number;
		this.outputRecipes=new ArrayList<ProductionRecipe>();
		this.useOnce=useOnce;
	}
	
	public ProductionRecipe(HashMap<Integer, ItemStack> input, ItemStack output, int batchAmount, String recipeName, 
			int productionTime,int number,boolean useOnce, HashMap <Enchantment, Integer> enchantments)
	{
		this(input,output,batchAmount,recipeName,productionTime,number,useOnce);
		this.enchantments = enchantments;
	}
	
	public void addOutputRecipe(ProductionRecipe outputRecipe)
	{
		this.outputRecipes.add(outputRecipe);
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
	
	public int getNumber()
	{
		return number;
	}

	public ArrayList<ProductionRecipe> getOutputRecipes()
	{
		return outputRecipes;
	}

	public boolean getUseOnce()
	{
		return useOnce;
	}
}
