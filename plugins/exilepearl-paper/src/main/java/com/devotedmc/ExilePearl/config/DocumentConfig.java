package com.devotedmc.ExilePearl.config;

/**
 * A document-based configuration file
 * 
 * @author Gordon
 */
public interface DocumentConfig {
	
	/**
	 * Gets a document with all the configuration data
	 * @return The configuration document
	 */
	Document getDocument();
	
	/**
	 * Reloads the configuration from file
	 * @return The SabreConfig instance
	 */
	DocumentConfig reloadFile();
	
	/**
	 * Saves the configuration to file
	 */
	void saveToFile();
	
	/**
	 * Saves a specific document to the configuration file
	 * @param doc The document to save
	 */
	void saveToFile(Document doc);
}
