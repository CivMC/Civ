package com.github.igotyou.FactoryMod.properties;

import java.util.List;

import com.github.igotyou.FactoryMod.interfaces.Properties;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;


public class ProductionProperties implements Properties
{
	private ItemList<NamedItemStack> inputs;
	private List<ProductionRecipe> recipes;
	private ItemList<NamedItemStack> fuel;
	private int energyTime;
	private String name;
	private int repair;
	
	public ProductionProperties(ItemList<NamedItemStack> inputs, List<ProductionRecipe> recipes,
			ItemList<NamedItemStack> fuel, int energyTime, String name,int repair)
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

	public ItemList<NamedItemStack> getInputs() 
	{
		return inputs;
	}
	
	public List<ProductionRecipe> getRecipes()
	{
		return recipes;
	}
	
	public ItemList<NamedItemStack> getFuel()
	{
		return fuel;
	}
	
	public int getEnergyTime()
	{
		return energyTime;
	}
	
	public String getName()
	{
		return name;
	}
	
}
