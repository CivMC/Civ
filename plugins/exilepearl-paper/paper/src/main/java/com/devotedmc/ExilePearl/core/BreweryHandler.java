package com.devotedmc.ExilePearl.core;

import com.devotedmc.ExilePearl.BrewHandler;
import com.dre.brewery.Brew;
import org.bukkit.inventory.ItemStack;

public class BreweryHandler implements BrewHandler {

	@Override
	public boolean isBrew(ItemStack item) {
		return Brew.get(item) != null;
	}

}
