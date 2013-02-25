package com.github.igotyou.FactoryMod.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;

import com.github.igotyou.FactoryMod.interfaces.Properties;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;

public class ProductionProperties implements Properties
{
	private HashMap<Integer, Material> buildMaterial;
	private HashMap<Integer, Integer> buildAmount;
	private List<ProductionRecipe> recipes;
	Material energyMaterial;
	int energyTime;
	int energyConsumption;
	String name;
	
	public ProductionProperties(HashMap<Integer, Material> buildMaterial, HashMap<Integer, Integer> buildAmount,
			List<ProductionRecipe> recipes, Material energyMaterial, int energyTime, int energyConsumption, String name)
	{
		this.buildMaterial = buildMaterial;
		this.buildAmount = buildAmount;
		this.recipes = recipes;
		this.energyMaterial = energyMaterial;
		this.energyTime = energyTime;
		this.energyConsumption = energyConsumption;
		this.name = name;
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
	
	public Material getEnergyMaterial()
	{
		return energyMaterial;
	}
	
	public int getEnergyTime()
	{
		return energyTime;
	}
}
