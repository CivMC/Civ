package com.github.igotyou.FactoryMod.recipes;

import java.util.List;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface IRecipe 
{	
	public String getRecipeName();
	
	public int getProductionTime();	
	
	public boolean enoughMaterialAvailable(Inventory i);
	
	public void applyEffect(Inventory i);
}
