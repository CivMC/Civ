package com.devotedmc.ExilePearl.config;

public interface Documentable {

	/**
	 * Gets a root key for the object.
	 * Configuration values for the object will be written to this key
	 * @return The object key
	 */
	public String getDocumentKey();
	
	/**
	 * Gets a document containing all the configuration data for this object
	 * @return The document
	 */
	public Document getDocument();
	
	/**
	 * Loads all the configuration data from a document
	 * @param doc The document to load
	 * @return The object instance
	 */
	public Documentable loadDocument(Document doc);
}
