package com.devotedmc.ExilePearl;

import java.util.logging.Level;

public interface PearlLogging {

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
