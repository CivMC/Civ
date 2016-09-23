package com.devotedmc.ExilePearl;

import java.util.UUID;
import java.util.logging.Level;

/**
 * External API for ExilePearl
 * @author Gordon
 *
 */
public interface ExilePearlApi extends PearlManager {
	
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

	/**
	 * Logs a message
	 * @param level The logging level
	 * @param msg The message
	 * @param args The message arguments
	 */
	void log(Level level, String msg, Object... args);
	
	/**
	 * Logs a message
	 * @param msg The message
	 * @param args The message arguments
	 */
	void log(String msg, Object... args);
}
