package com.github.igotyou.FactoryMod.interfaces;

public interface Recipe 
{	
	//get the recipes name, example: Iron Pickaxe
	public String getRecipeName();
	
	//get production time in update cycles
	public int getProductionTime();	
}
