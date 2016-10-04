package com.devotedmc.ExilePearl;

import java.util.UUID;

import org.bukkit.entity.Player;

/**
 * An interface for providing the real names of players
 * @author Gordon
 */
public interface PlayerProvider {
	
	/**
	 * Gets a bukkit player instance
	 * @param uid The player ID
	 * @return The player instance if it exists
	 */
	Player getPlayer(UUID uid);
	
	/**
	 * Gets a bukkit player instance
	 * @param name The name of the player
	 * @return The player instance if it exists
	 */
	Player getPlayer(String name);
	
	/**
	 * Gets a player name from a UUID
	 * @param uuid The given player UUID
	 * @return The matching player name
	 */
	String getName(UUID uid);
	
	/**
	 * Gets a player UUID from a name
	 * @param name The given player name
	 * @return The matching player UUID
	 */
	UUID getUniqueId(String name);

}
