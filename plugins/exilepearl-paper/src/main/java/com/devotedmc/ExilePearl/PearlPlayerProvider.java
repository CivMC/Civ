package com.devotedmc.ExilePearl;

import java.util.UUID;

public interface PearlPlayerProvider {

	/**
	 * Gets a pearl player instance by UUID
	 * @param uid The player UUID
	 * @return The pearl player instance
	 */
	PearlPlayer getPearlPlayer(UUID uid);
}
