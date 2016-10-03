package com.devotedmc.ExilePearl;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.devotedmc.ExilePearl.util.ExilePearlTask;

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
	
	/**
	 * Creates a pearl manager instance
	 * @return The new pearl manager instance
	 */
	PearlManager createPearlManager();
	
	/**
	 * Creates a pearl worker instance
	 * @return The new pearl worker instance
	 */
	ExilePearlTask createPearlDecayWorker();
	
	/**
	 * Creates the suicide handler instance
	 * @return The new suicide handler instance
	 */
	SuicideHandler createSuicideHandler();
	
	/**
	 * Creates a new PearlPlayer instance
	 * @param uid The player ID
	 * @return The new PearlPlayer instance
	 */
	PearlPlayer createPearlPlayer(UUID uid);
	
	/**
	 * Creates the lore generator instance
	 * @return The lore generator instance
	 */
	PearlLoreGenerator createLoreGenerator();
	
	/**
	 * Creates the pearl config instance
	 * @return The pearl config instance
	 */
	PearlConfig createPearlConfig();
}
