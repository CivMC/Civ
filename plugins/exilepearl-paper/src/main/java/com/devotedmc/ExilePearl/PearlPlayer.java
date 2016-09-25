package com.devotedmc.ExilePearl;

import java.util.Set;

import org.bukkit.entity.Player;

/**
 * Extension interface for the Bukkit Player class that adds
 * ExilePearl specific functionality
 * @author Gordon
 *
 */
public interface PearlPlayer extends Player {
	
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
	 * @param sp The broadcast player
	 */
	void addBcastPlayer(PearlPlayer sp);
	
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
}
