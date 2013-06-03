package com.github.igotyou.FactoryMod.properties;

import java.util.List;

import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;

public class PrintingPressProperties {

	private ItemList<NamedItemStack> fuel;
	private ItemList<NamedItemStack> constructionMaterials;
	private int energyTime;
	private String name;
	private int maxRepair;
	
	public PrintingPressProperties(	ItemList<NamedItemStack> fuel, ItemList<NamedItemStack> constructionMaterials, int energyTime, String name, int repair)
	{
		this.fuel = fuel;
		this.energyTime = energyTime;
		this.name = name;
		this.maxRepair=repair;
		this.constructionMaterials = constructionMaterials;
	}

	public int getMaxRepair()
	{
		return maxRepair;
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

	public ItemList<NamedItemStack> getConstructionMaterials() {
		return constructionMaterials;
	}
}
