package com.devotedmc.ExilePearl;

public interface PearlManager extends PearlAccess {

	/**
	 * Loads all pearls from storage
	 */
	void loadPearls();
	
	/**
	 * Performs a health decay operation on all pearls
	 */
	public void decayPearls();
}
