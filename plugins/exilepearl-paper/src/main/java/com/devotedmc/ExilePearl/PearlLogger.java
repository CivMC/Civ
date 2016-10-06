package com.devotedmc.ExilePearl;

import java.util.logging.Level;
import java.util.logging.Logger;

public interface PearlLogger {

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
	
	/**
	 * Gets the raw logger instance
	 * @return The logger instance
	 */
	Logger getPluginLogger();
}
