package com.devotedmc.ExilePearl.config;

/**
 * An object that can be loaded with configuration data
 * 
 * @author Gordon
 */
public interface Configurable {
	
	/**
	 * Loads configuration data
	 * @param config The config
	 */
	public void loadConfig(PearlConfig config);
}
