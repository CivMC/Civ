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
	 * @param location The location of the pearl
	 * @return The new exile pearl instance
	 */
	ExilePearl createExilePearl(UUID uid, UUID killedBy, Location location);
	
	/**
	 * Creates an exile pearl instance from a player holder
	 * @param uid The prisoner UUID
	 * @param killedBy The killing player
	 * @param health The pearl health
	 * @return The new exile pearl instance
	 */
	ExilePearl createExilePearl(UUID uid, Player killedBy, int health);
	
	/**
	 * Creates a pearl manager instance
	 * @return The new pearl manager instance
	 */
	PearlManager createPearlManager();
	
	/**
	 * Creates a pearl worker instance
	 * @return The new pearl worker instance
	 */
	PearlWorker createPearlWorker();
	
	/**
	 * Creates a new PearlPlayer instance
	 * @param player The bukkit player instance
	 * @return The new PearlPlayer instance
	 */
	PearlPlayer createPearlPlayer(Player player);
	
	/**
	 * Creates the lore generator instance
	 * @return The lore generator instance
	 */
	PearlLoreGenerator createLoreGenerator();
}
