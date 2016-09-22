package com.devotedmc.ExilePearl.storage;

import java.util.Date;

public interface PluginStorage extends PearlStorage {


	/**
	 * Updates the last pearl feed time
	 * @param now The current time
	 */
	public void updateLastFeedTime(Date now);
	
	/**
	 * Gets the last feed time
	 * @return The last feed time
	 */
	public Date getLastFeedTime();
}
