package com.devotedmc.ExilePearl;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Factory interface for creating concrete pearl classes
 * @author Gordon
 */
public interface PearlFactory {

	/**
	 * Creates an exile pearl instance from a location
	 * @param uid The prisoner UUID
	 * @param killedBy The killing player UUID
	 * @param pearlId The pearl Id
	 * @param location The location of the pearl
	 * @return The new exile pearl instance
	 */
	ExilePearl createExilePearl(UUID uid, UUID killedBy, int pearlId, Location location);
	
	/**
	 * Creates an exile pearl instance from a player holder
	 * @param uid The prisoner UUID
	 * @param killedBy The killing player
	 * @param pearlId The pearl Id
	 * @return The new exile pearl instance
	 */
	ExilePearl createExilePearl(UUID uid, Player killedBy, int pearlId);
}
