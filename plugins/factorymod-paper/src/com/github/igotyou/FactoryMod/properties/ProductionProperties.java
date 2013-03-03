package com.github.igotyou.FactoryMod.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.interfaces.Properties;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;

public class ProductionProperties implements Properties
{
	private HashMap<Integer, ItemStack> buildMaterials;
	private List<ProductionRecipe> recipes;
	ItemStack energyMaterial;
	int energyTime;
	String name;
	
	public ProductionProperties(HashMap<Integer, ItemStack> buildMaterials, List<ProductionRecipe> recipes,
			ItemStack energyMaterial, int energyTime, String name)
	{
		this.buildMaterials = buildMaterials;
		this.recipes = recipes;
		this.energyMaterial = energyMaterial;
		this.energyTime = energyTime;
		this.name = name;
	}

	public HashMap<Integer, ItemStack> getBuildMaterials() 
	{
		return buildMaterials;
	}
	
	public List<ProductionRecipe> getRecipes()
	{
		return recipes;
	}
	
	public ItemStack getEnergyMaterial()
	{
		return energyMaterial;
	}
	
	public int getEnergyTime()
	{
		return energyTime;
	}
}
