package com.devotedmc.ExilePearl;

/**
 * Reasons for a pearl being freed
 * @author Gordon
 *
 */
public enum PearlFreeReason
{
	FREED_BY_PLAYER,
	PEARL_THROWN,
	FREED_BY_ADMIN,
	FORCE_FREED_BY_ADMIN,
	HEALTH_DECAY,
	PEARL_DESTROYED,
	VALIDATION_FAILED,
	OUTSIDE_WORLD_BORDER,
	FREED_OFFLINE
	;
}
