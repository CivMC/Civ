package com.devotedmc.ExilePearl.core;

import com.devotedmc.ExilePearl.BrewHandler;
import com.devotedmc.ExilePearl.ExilePearlApi;
import org.bukkit.inventory.ItemStack;

public class NoBrewHandler implements BrewHandler {

	public NoBrewHandler(ExilePearlApi pearlApi) {
		// no-op
	}

	@Override
	public boolean isBrew(ItemStack item) {
		return false;
	}

}
