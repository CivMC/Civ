package com.devotedmc.ExilePearl.broadcast;

import com.devotedmc.ExilePearl.ExilePearl;

/**
 * Interface for a target that is receiving pearl broadcast updates
 * @author Gordon
 *
 */
public interface PearlBroadcastTarget {
	
	/**
	 * Broadcasts the pearl location
	 * @param pearl The pearl to broadcast
	 */
	void broadcast(ExilePearl pearl);

}
