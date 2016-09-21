package com.devotedmc.ExilePearl;

import java.util.UUID;

import org.bukkit.entity.Player;

/**
 * External API for ExilePearl
 * @author Gordon
 *
 */
public interface ExilePearlApi {

	/**
	 * Gets whether a player is exiled
	 * @param player The player to check
	 * @return true if the player is exiled
	 */
	boolean isPlayerExiled(Player player);
	
	/**
	 * Gets whether a player is exiled
	 * @param uid The player UUID to check
	 * @return true if the player is exiled
	 */
	boolean isPlayerExiled(UUID uid);
}
