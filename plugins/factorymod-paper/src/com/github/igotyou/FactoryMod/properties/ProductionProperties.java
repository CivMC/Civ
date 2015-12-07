package com.github.igotyou.FactoryMod.properties;

import java.util.List;

import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.AdvancedItemStack;


public class ProductionProperties extends AFactoryProperties
{
	private ItemList<AdvancedItemStack> inputs;
	private List<ProductionRecipe> recipes;
	private ItemList<AdvancedItemStack> fuel;
	private int energyTime;
	private int repair;
	
	public ProductionProperties(ItemList<AdvancedItemStack> inputs, List<ProductionRecipe> recipes,
			ItemList<AdvancedItemStack> fuel, int energyTime, String name,int repair)
	{
		this.inputs = inputs;
		this.recipes = recipes;
		this.fuel = fuel;
		this.energyTime = energyTime;
		this.name = name;
		this.repair=repair;
	}

	public int getRepair()
	{
		return repair;
	}

	public ItemList<AdvancedItemStack> getInputs() 
	{
		return inputs;
	}
	
	public List<ProductionRecipe> getRecipes()
	{
		return recipes;
	}
	
	public ItemList<AdvancedItemStack> getFuel()
	{
		return fuel;
	}
	
	public int getEnergyTime()
	{
		return energyTime;
	}
	
}
