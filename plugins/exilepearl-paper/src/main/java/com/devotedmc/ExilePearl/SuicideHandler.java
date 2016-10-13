package com.devotedmc.ExilePearl;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.devotedmc.ExilePearl.util.ExilePearlRunnable;

public interface SuicideHandler extends ExilePearlRunnable, Listener {
	
	/**
	 * Adds a player to the suicide queue
	 * @param player The player to add
	 */
	void addPlayer(Player player);

	/**
	 * Gets whether a player is added
	 * @param uid The player to check
	 * @return true if the player is added
	 */
	boolean isAdded(UUID uid);
}
