package com.devotedmc.ExilePearl.storage;

import com.devotedmc.ExilePearl.ExilePearl;

public interface PearlUpdateStorage {
	
	/**
	 * Updates the location of the pearl
	 * @param pearl The pearl instance to update
	 */
	void pearlUpdateLocation(ExilePearl pearl);
	
	/**
	 * Updates the pearl health
	 * @param pearl The pearl instance to update
	 */
	void pearlUpdateHealth(ExilePearl pearl);
	
	/**
	 * Updates the freed offline status
	 * @param pearl The pearl instance to update
	 */
	void pearlUpdateFreedOffline(ExilePearl pearl);
}
