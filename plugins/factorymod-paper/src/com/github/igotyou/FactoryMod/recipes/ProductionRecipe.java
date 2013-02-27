package com.github.igotyou.FactoryMod.recipes;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import com.github.igotyou.FactoryMod.interfaces.Recipe;

public class ProductionRecipe implements Recipe
{
	private HashMap <Integer, Material> inputMaterial;
	private HashMap <Integer, Integer> inputAmount;
	private Material output;
	private HashMap <Enchantment, Integer> enchantments;
	private short durability = 0;
	private int batchAmount;
	private int productionTime;
	private String recipeName;
	
	public ProductionRecipe(HashMap<Integer, Material> inputMaterial, HashMap<Integer,Integer> inputAmount, Material output,
			int batchAmount, String recipeName, int productionTime)
	{
		this.inputMaterial = inputMaterial;
		this.inputAmount = inputAmount;
		this.output = output;
		this.batchAmount = batchAmount;
		this.recipeName = recipeName;
		this.productionTime = productionTime;
	}
	
	public ProductionRecipe(HashMap<Integer, Material> inputMaterial, HashMap<Integer,Integer> inputAmount, Material output,
			int batchAmount, String recipeName, int productionTime, HashMap <Enchantment, Integer> enchantments)
	{
		this.inputMaterial = inputMaterial;
		this.inputAmount = inputAmount;
		this.output = output;
		this.batchAmount = batchAmount;
		this.recipeName = recipeName;
		this.productionTime = productionTime;
		this.enchantments = enchantments;
	}
	
	public ProductionRecipe(HashMap<Integer, Material> inputMaterial, HashMap<Integer,Integer> inputAmount, Material output,
			int batchAmount, String recipeName, int productionTime, short durability)
	{
		this.inputMaterial = inputMaterial;
		this.inputAmount = inputAmount;
		this.output = output;
		this.batchAmount = batchAmount;
		this.recipeName = recipeName;
		this.productionTime = productionTime;
		this.durability = durability;
	}
	
	public ProductionRecipe(HashMap<Integer, Material> inputMaterial, HashMap<Integer,Integer> inputAmount, Material output,
			int batchAmount, String recipeName, int productionTime, HashMap <Enchantment, Integer> enchantments,
			short durability)
	{
		this.inputMaterial = inputMaterial;
		this.inputAmount = inputAmount;
		this.output = output;
		this.batchAmount = batchAmount;
		this.recipeName = recipeName;
		this.productionTime = productionTime;
		this.enchantments = enchantments;
		this.durability = durability;
	}
	
	public int getBatchAmount() 
	{
		return batchAmount;
	}

	public Material getOutput() 
	{
		return output;
	}

	public HashMap<Integer, Material> getInputMaterial() 
	{
		return inputMaterial;
	}

	public HashMap<Integer, Integer> getInputAmount() 
	{
		return inputAmount;
	}
	
	public HashMap<Enchantment, Integer> getEnchantments()
	{
		return enchantments;
	}
	
	public short getDurability()
	{
		return durability;
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
