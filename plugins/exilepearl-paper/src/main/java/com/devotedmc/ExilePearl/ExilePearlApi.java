package com.devotedmc.ExilePearl;

import java.util.UUID;

/**
 * External API for ExilePearl
 * @author Gordon
 *
 */
public interface ExilePearlApi extends PearlAccess, PearlLogger {
	
	/**
	 * Gets a player instance by UUID
	 * @param uniqueId The player UUID
	 * @return The player instance
	 */
	PearlPlayer getPearlPlayer(final UUID uid);
	
	/**
	 * Gets a player instance by name
	 * @param uniqueId The player name
	 * @return The player instance
	 */
	PearlPlayer getPearlPlayer(final String name);
}
