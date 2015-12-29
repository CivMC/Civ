package com.github.igotyou.FactoryMod.recipes;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;

import com.github.igotyou.FactoryMod.utility.ItemMap;

public class DeterministicEnchantingRecipe {
	private Enchantment enchant;

	public DeterministicEnchantingRecipe(String name, int productionTime,
			ItemMap input, Enchantment enchant) {
		//super(name, productionTime, input);
		this.enchant = enchant;
	}
	
	public boolean enoughMaterialAvailable(Inventory i) {
		//TODO TODO TODO
		
		
		return false;	
	}

}
