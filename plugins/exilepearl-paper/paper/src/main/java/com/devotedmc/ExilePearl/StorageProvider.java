package com.devotedmc.ExilePearl;

import com.devotedmc.ExilePearl.storage.PluginStorage;

/**
 * Provides late-binding access to the storage object
 * @author Gordon
 *
 */
public interface StorageProvider {

	/**
	 * Gets the plugin storage instance
	 * @return The plugin storage instance
	 */
	PluginStorage getStorage();
}
