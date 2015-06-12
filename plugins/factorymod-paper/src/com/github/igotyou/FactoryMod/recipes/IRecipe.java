package com.github.igotyou.FactoryMod.recipes;

public interface IRecipe 
{	
	//get the recipes name, example: Iron Pickaxe
	public String getRecipeName();
	
	//get production time in update cycles
	public int getProductionTime();	
}
