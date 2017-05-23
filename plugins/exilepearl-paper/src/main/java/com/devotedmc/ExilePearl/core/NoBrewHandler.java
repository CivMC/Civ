package com.devotedmc.ExilePearl.core;

import org.bukkit.inventory.ItemStack;

import com.devotedmc.ExilePearl.BrewHandler;
import com.devotedmc.ExilePearl.ExilePearlApi;

public class NoBrewHandler implements BrewHandler {

	public NoBrewHandler(ExilePearlApi pearlApi) {
		// no-op
	}
	
	@Override
	public boolean isBrew(ItemStack item) {
		return false;
	}

}
