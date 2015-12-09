package com.github.igotyou.FactoryMod.recipes;

import org.bukkit.inventory.ItemStack;

public interface IRecipe 
{	
	public String getRecipeName();
	
	public int getProductionTime();	
	
	public ItemStack getItemRepresentation();
}
