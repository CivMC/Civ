package com.github.igotyou.FactoryMod.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;

import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import com.github.igotyou.interfaces.Properties;

public class ProductionProperties implements Properties
{
	private HashMap<Integer, Material> buildMaterial;
	private HashMap<Integer, Integer> buildAmount;
	private List<ProductionRecipe> recipes;
	int energyConsumption; 
	
	public ProductionProperties(HashMap<Integer, Material> buildMaterial, HashMap<Integer, Integer> buildAmount,
			ArrayList<ProductionRecipe> recipes, int energyConsumption)
	{
		this.buildMaterial = buildMaterial;
		this.buildAmount = buildAmount;
		this.recipes = recipes;
		this.energyConsumption = energyConsumption;
	}

	public HashMap<Integer, Integer> getBuildAmount() 
	{
		return buildAmount;
	}

	public HashMap<Integer, Material> getBuildMaterial() 
	{
		return buildMaterial;
	}
	
	public List<ProductionRecipe> getRecipes()
	{
		return recipes;
	}

	public int getEnergyConsumption()
	{
		return energyConsumption;
	}
}
