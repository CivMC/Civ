package com.devotedmc.ExilePearl;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.devotedmc.ExilePearl.util.BukkitTask;

public interface SuicideHandler extends BukkitTask, Listener {
	
	/**
	 * Adds a player to the suicide queue
	 * @param player The player to add
	 */
	void addPlayer(PearlPlayer player);

	/**
	 * Gets whether a player is added
	 * @param player The player to check
	 * @return true if the player is added
	 */
	boolean isAdded(Player player);
}
