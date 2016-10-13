package com.devotedmc.ExilePearl.config;

/**
 * A document-based configuration
 * 
 * @author Gordon
 */
public interface DocumentConfig {
	
	/**
	 * Gets the raw document with all the configuration data
	 * @return The configuration document
	 */
	Document getDocument();
	
	/**
	 * Reloads the configuration from file
	 */
	void reload();
	
	/**
	 * Saves the configuration to file
	 */
	void saveToFile();
	
	/**
	 * Registers a configurable object
	 * @param configurable The object to register
	 */
	void addConfigurable(Configurable configurable);
}
