package com.devotedmc.ExilePearl.core;

import org.bukkit.inventory.ItemStack;

import com.devotedmc.ExilePearl.BrewHandler;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.dre.brewery.Brew;

public class BreweryHandler implements BrewHandler {
	private ExilePearlApi pearlApi;
	public BreweryHandler(ExilePearlApi pearlApi) {
		this.pearlApi = pearlApi;
	}
	
	@Override
	public boolean isBrew(ItemStack item) {
		pearlApi.log("Testing {0} found {1} uid {2}", item, Brew.get(item), Brew.getUID(item));
		return Brew.get(item) != null;
	}

}
