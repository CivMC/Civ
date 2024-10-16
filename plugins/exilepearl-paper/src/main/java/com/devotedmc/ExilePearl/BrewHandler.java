package com.devotedmc.ExilePearl;

import org.bukkit.inventory.ItemStack;

/**
 * Went the Interface contract route in case future coders want better descrimination
 * around Brewing capabilities. For instance, if they can consume but not brew, etc.
 * 
 * @author ProgrammerDan
 *
 */
public interface BrewHandler {

	/**
	 * Base handler description for checking if something is not a vanilla potion, but
	 * rather a plugin brew.
	 * @param item The item to check
	 * @return true if a plugin brew, false otherwise.
	 */
	public boolean isBrew(ItemStack item);
}
