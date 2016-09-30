package com.devotedmc.ExilePearl.util;

public interface BukkitTask {

	/**
	 * Start the pearl worker
	 */
	void start();
	
	/**
	 * Stop the pearl worker
	 */
	void stop();
	
	/**
	 * Restart the pearl worker
	 */
	void restart();
	
	/**
	 * Gets whether the task is running
	 * @return true if the task is running
	 */
	boolean isRunning();
}
