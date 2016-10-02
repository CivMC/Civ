package com.devotedmc.ExilePearl;

import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

/**
 * Extension interface for the Bukkit Player class that adds
 * ExilePearl specific functionality
 * @author Gordon
 *
 */
public interface PearlPlayer {
	
	/**
	 * Gets the player name
	 * @return The player name
	 */
	String getName();
	
	/**
	 * Gets the player ID
	 * @return The player ID
	 */
	UUID getUniqueId();
	
	/**
	 * Gets whether the player is online
	 * @return true if the player is online
	 */
	boolean isOnline();
	
	/**
	 * Gets the bukkit player instance
	 * @return
	 */
	Player getPlayer();
	
	/**
	 * Sends a formatted message to the player
	 * @param str The message
	 * @param args The message arguments
	 */
	void msg(final String str, final Object... args);
	
	/**
	 * Gets the broadcasting players
	 * @return The broadcast players
	 */
	Set<PearlPlayer> getBcastPlayers();
	
	/**
	 * Adds a broadcast player
	 * @param sp The broadcast player to add
	 */
	void addBcastPlayer(PearlPlayer sp);
	
	/**
	 * Removes a broadcast player
	 * @param sp The broadcast player to remove
	 */
	void removeBcastPlayer(PearlPlayer sp);
	
	/**
	 * Gets the player requested to broadcast pearl location
	 * @return the requested broadcast player
	 */
	PearlPlayer getRequestedBcastPlayer();
	
	/**
	 * Sets the requested broadcast player
	 * @param broadcastRequestPlayer the requested broadcast player
	 */
	void setRequestedBcastPlayer(PearlPlayer broadcastRequestPlayer);
	
	/**
	 * Gets whether the player is exiled
	 * @return true if the player is exiled
	 */
	boolean isExiled();
	
	/**
	 * Gets the player's exile pearl instance if it exists
	 * @return The exile pearl if it exists, otherwise null
	 */
	ExilePearl getExilePearl();
}
