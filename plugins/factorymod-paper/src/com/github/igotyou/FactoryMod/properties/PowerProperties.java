package com.github.igotyou.FactoryMod.properties;

import java.util.HashMap;

import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.interfaces.Properties;

public class PowerProperties implements Properties
{
	private HashMap<Integer, ItemStack> buildMaterials;
	
	public PowerProperties(HashMap<Integer, ItemStack> buildMaterials)
	{
		this.buildMaterials = buildMaterials;
	}
	
	public HashMap<Integer, ItemStack> getBuildMaterials() 
	{
		return buildMaterials;
	}

}
