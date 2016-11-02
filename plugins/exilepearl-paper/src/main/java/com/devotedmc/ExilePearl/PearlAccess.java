package com.devotedmc.ExilePearl;

import java.util.Collection;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.devotedmc.ExilePearl.holder.PearlHolder;

public interface PearlAccess {
	
	/**
	 * Binds a player to an exile pearl
	 * @param exiledId The exiled player Id
	 * @param killerId The killing player ID
	 * @param holder The pearl holder
	 * @return The new ExilePearl if the operation succeeds, otherwise null
	 */
	ExilePearl exilePlayer(UUID exiledId, UUID killerId, PearlHolder holder);
	
	/**
	 * Binds a player to an exile pearl
	 * @param exiledId The exiled player Id
	 * @param killerId The killing player ID
	 * @param location The location to place the pearl
	 * @return The new ExilePearl if the operation succeeds, otherwise null
	 */
	ExilePearl exilePlayer(UUID exiledId, UUID killerId, Location location);
	
	/**
	 * Binds a player to an exile pearl
	 * @param exiledId The exiled player Id
	 * @param killer The killing player
	 * @return The new ExilePearl if the operation succeeds, otherwise null
	 */
	ExilePearl exilePlayer(UUID exiledId, Player killer);
	
	/**
	 * Gets a pearl instance by name
	 * @param name The name to search for
	 * @return The instance if it is found, otherwise null
	 */
	ExilePearl getPearl(String name);
	
	/**
	 * Gets a pearl instance by UUID
	 * @param uid The UUID to search for
	 * @return The instance if it is found, otherwise null
	 */
	ExilePearl getPearl(UUID uid);
	
	/**
	 * Gets the collection of all pearled instances.
	 * This collection is not modifiable.
	 * @return The pearl instance collection
	 */
	Collection<ExilePearl> getPearls();

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
	 * @param reason The free reason
	 * @return true if the pearl is freed, otherwise false
	 */
	boolean freePearl(ExilePearl pearl, PearlFreeReason reason);
}
