package com.github.igotyou.FactoryMod.properties;

import java.util.Map;

import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.interfaces.Properties;



public class PowerProperties implements Properties
{
	private Map<ItemStack,String> buildMaterials;
	
	public PowerProperties(Map<ItemStack,String> buildMaterials)
	{
		this.buildMaterials = buildMaterials;
	}
	
	public Map<ItemStack,String> getBuildMaterials() 
	{
		return buildMaterials;
	}

}
