package com.devotedmc.ExilePearl;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.devotedmc.ExilePearl.config.Document;

/**
 * Factory interface for creating concrete pearl classes
 * @author Gordon
 */
public interface PearlFactory {
	
	/**
	 * Creates an exile pearl instance from a player holder
	 * @param uid The prisoner UUID
	 * @param killedBy The killing player
	 * @param pearlId The pearl Id
	 * @return The new exile pearl instance
	 */
	ExilePearl createExilePearl(UUID uid, Player killedBy, int pearlId);

	/**
	 * Creates an exile pearl instance from a location
	 * @param uid The prisoner UUID
	 * @param doc The document containing the pearl data
	 * @return The new exile pearl instance
	 */
	ExilePearl createExilePearl(UUID uid, Document doc);
	
	/**
	 * Creates a migrated pearl instance
	 * @param uid The prisoner UUID
	 * @param doc The document containing the pearl data
	 * @return The new exile pearl instance
	 */
	ExilePearl createdMigratedPearl(UUID uid, Document doc);
}
