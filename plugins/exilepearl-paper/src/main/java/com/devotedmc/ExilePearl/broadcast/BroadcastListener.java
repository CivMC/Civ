package com.devotedmc.ExilePearl.broadcast;

import com.devotedmc.ExilePearl.ExilePearl;

/**
 * Interface for a target that is receiving pearl broadcast updates
 * @author Gordon
 *
 */
public interface BroadcastListener {
	
	/**
	 * Broadcasts the pearl location
	 * @param pearl The pearl to broadcast
	 */
	void broadcast(ExilePearl pearl);
	
	/**
	 * Gets whether the broadcast listener contains an underlying object
	 * @param o the object to check
	 * @return true if it exists
	 */
	boolean contains(Object o);

}
