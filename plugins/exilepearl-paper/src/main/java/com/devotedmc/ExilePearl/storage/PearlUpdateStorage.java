package com.devotedmc.ExilePearl.storage;

import com.devotedmc.ExilePearl.ExilePearl;

public interface PearlUpdateStorage {
	
	/**
	 * Updates the location of the pearl
	 * @param pearl The pearl instance to update
	 */
	void updatePearlLocation(ExilePearl pearl);
	
	/**
	 * Updates the pearl health
	 * @param pearl The pearl instance to update
	 */
	void updatePearlHealth(ExilePearl pearl);
	
	/**
	 * Updates the freed offline status
	 * @param pearl The pearl instance to update
	 */
	void updatePearlFreedOffline(ExilePearl pearl);
	
	/**
	 * Updates the pearl type
	 * @param pearl The pearl instance to update
	 */
	void updatePearlType(ExilePearl pearl);
	
	/**
	 * Updates the pearl killer
	 * @param pearl The pearl instance to update
	 */
	void updatePearlKiller(ExilePearl pearl);
	
	/**
	 * Updates the last online time
	 * @param pearl The pearl instance to update
	 */
	void updatePearlLastOnline(ExilePearl pearl);
}
