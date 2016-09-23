package com.devotedmc.ExilePearl.storage;

public interface PluginStorage extends PearlStorage {
	
	/**
	 * Connects the storage
	 * @return true if connection succeeds
	 */
	public boolean connect();
	
	/**
	 * Disconnects the storage
	 * @return true if disconnect succeeds
	 */
	public boolean disconnect();
	
	/**
	 * Gets whether the storage is connected
	 * @return true if connected
	 */
	public boolean isConnected();
}
