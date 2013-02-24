package com.github.igotyou.FactoryMod.recipes;

import java.util.HashMap;

import org.bukkit.Material;

import com.github.igotyou.interfaces.Recipe;

public class ProductionRecipe implements Recipe
{
	private HashMap <Integer, Material> inputMaterial;
	private HashMap <Integer, Integer> inputAmount;
	private Material output;
	private int batchAmount;
	private int productionTime;
	private String recipeName;
	
	public ProductionRecipe(HashMap<Integer, Material> inputMaterial, HashMap<Integer,Integer> inputAmount, Material output,
			int batchAmount, String recipeName)
	{
		this.inputMaterial = inputMaterial;
		this.inputAmount = inputAmount;
		this.output = output;
		this.batchAmount = batchAmount;
		this.recipeName = recipeName;
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
