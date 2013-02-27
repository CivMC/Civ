package com.github.igotyou.FactoryMod.recipes;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.interfaces.Recipe;
import com.github.igotyou.FactoryMod.properties.ProductionProperties;

public class ProductionRecipe implements Recipe
{
	private HashMap <Integer, Material> inputMaterial;
	private HashMap <Integer, Integer> inputAmount;
	private Material output;
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
		
	public String getRecipeName() 
	{
		return recipeName;
	}

	public int getProductionTime() 
	{
		return productionTime;
	}



}
