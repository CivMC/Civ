package com.devotedmc.ExilePearl.core;

import org.bukkit.inventory.ItemStack;

import com.devotedmc.ExilePearl.BrewHandler;
import com.dre.brewery.Brew;

public class BreweryHandler implements BrewHandler {
	
	@Override
	public boolean isBrew(ItemStack item) {
		return Brew.get(item) != null;
	}

}
