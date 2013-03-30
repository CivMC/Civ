package com.github.igotyou.FactoryMod.properties;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.interfaces.Properties;


public class PowerProperties implements Properties
{
	private List<ItemStack> buildMaterials;
	
	public PowerProperties(List<ItemStack> buildMaterials)
	{
		this.buildMaterials = buildMaterials;
	}
	
	public List<ItemStack> getBuildMaterials() 
	{
		return buildMaterials;
	}

}
