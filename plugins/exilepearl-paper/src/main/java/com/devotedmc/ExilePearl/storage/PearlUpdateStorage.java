package com.devotedmc.ExilePearl.storage;

import com.devotedmc.ExilePearl.ExilePearl;

public interface PearlUpdateStorage {
	
	/**
	 * Updates the location of the pearl
	 * @param pearl The pearl instance to update
	 */
	void pearlUpdateLocation(ExilePearl pearl);
	
	/**
	 * Updates the pearl strength
	 * @param strength The pearl instance to udpate
	 */
	void pearlUpdateStrength(ExilePearl strength);
}
