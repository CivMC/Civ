package com.devotedmc.ExilePearl;

import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.devotedmc.ExilePearl.config.Configurable;
import com.devotedmc.ExilePearl.util.ExilePearlRunnable;

public interface DamageLogger extends Configurable, Listener, ExilePearlRunnable {
	
	/**
	 * Records damage that a player deals to another player
	 * @param player The player being harmed
	 * @param damager The damage dealer
	 * @param amount The damage amount
	 */
	void recordDamage(Player player, Player damager, double amount); 
	
	/**
	 * Gets a sorted list of damaging players.
	 * 
	 * The player at the front of the collection is the one that should be 
	 * awarded the pearl according to the configured algorithm.
	 *  
	 * @param playerId The player ID being harmed
	 * @return The damaging players
	 */
	List<Player> getSortedDamagers(UUID playerId);
	
	/**
	 * Gets a sorted list of damaging players.
	 * 
	 * The player at the front of the collection is the one that should be 
	 * awarded the pearl according to the configured algorithm.
	 *  
	 * @param player The player being harmed
	 * @return The damaging players
	 */
	List<Player> getSortedDamagers(Player player);

}
