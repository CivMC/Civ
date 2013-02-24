package com.github.igotyou.FactoryMod.interfaces;

import java.util.HashMap;

import org.bukkit.Material;

public interface Recipe 
{
	//how many output do we produce at once?
	public int getBatchAmount();
	
	//the output of this recipe
	public Material getOutput();
	
	//get the materials needed for one output
	public HashMap<Integer,Material> getInputMaterial();
	
	//get the amount of above material nedded
	public HashMap<Integer,Integer> getInputAmount();
	
	//get the recipes name, example: Iron Pickaxe
	public String getRecipeName();
	
	//get production time in update cycles
	public int getProductionTime();
	
}
