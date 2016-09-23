package com.devotedmc.ExilePearl;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface PearlManager {
	
	/**
	 * Binds a player to an exile pearl
	 * @param exiled The player to exile
	 * @param killedBy The killing player
	 * @return The new ExilePearl if the operation succeeds, otherwise null
	 */
	ExilePearl exilePlayer(Player exiled, Player killedBy);
	
	/**
	 * Gets a pearl instance by UUID
	 * @param uid The UUID to search for
	 * @return The instance if it is found, otherwise null
	 */
	ExilePearl getPearl(UUID uid);

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
	
	/**
	 * Gets an exile pearl instance from an item stack
	 * if it is valid 
	 * @param is The item stack to check
	 * @return The pearl instance if it exists, otherwise null
	 */
	ExilePearl getPearlFromItemStack(ItemStack is);
	
	/**
	 * Frees an exile pearl
	 * @param pearl The pearl to free
	 * @return true if the pearl is freed, otherwise false
	 */
	boolean freePearl(ExilePearl pearl);
}
