package com.github.igotyou.FactoryMod.interfaces;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public interface Recipe 
{
	//how many output do we produce at once?
	public int getBatchAmount();
	
	//the output of this recipe
	public ItemStack getOutput();
	
	public HashMap<Integer, ItemStack> getInput();
		
	//get the recipes name, example: Iron Pickaxe
	public String getRecipeName();
	
	//get production time in update cycles
	public int getProductionTime();

	public Map<Enchantment, Integer> getEnchantments();
	
}
