package com.devotedmc.ExilePearl;

import java.util.Collection;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.devotedmc.ExilePearl.holder.PearlHolder;

/**
 * Interface for exiling players and checking exile statuses
 * @author Gordon
 */
public interface PearlAccess {
	
	/**
	 * Binds a player to an exile pearl with the resulting pearl being placed
	 * in the given {@link PearlHolder} instance.
	 * <p>
	 * It's possible for the exile operation to fail, so the return value
	 * should be checked for success. A non-null value indicates success.
	 * <p>
	 * An exile operation can fail and return null for several reasons:
	 * <ul>
	 * <li> The player to be exiled is already exiled
	 * <li> The {@link com.devotedmc.ExilePearl.event.PlayerPearledEvent PlayerPearledEvent} is canceled by another plugin
	 * </ul>
	 * @param exiledId The exiled player ID
	 * @param killerId The killing player ID
	 * @param holder The pearl holder
	 * @return The new {@link ExilePearl} instance if the operation succeeds, 
	 * otherwise null
	 */
	ExilePearl exilePlayer(UUID exiledId, UUID killerId, PearlHolder holder);
	
	/**
	 * Binds a player to an exile pearl with the resulting pearl being placed
	 * in the inventory of a specific location.
	 * <p>
	 * This method will return null if the block at the given location doesn't
	 * contain an inventory.
	 * @see PearlAccess#exilePlayer(UUID, UUID, PearlHolder)
	 * @param exiledId The exiled player ID
	 * @param killerId The killing player ID
	 * @param location The location to place the pearl
	 * @return The new {@link ExilePearl} instance if the operation succeeds, 
	 * otherwise null
	 */
	ExilePearl exilePlayer(UUID exiledId, UUID killerId, Location location);
	
	/**
	 * Binds a player to an exile pearl with the resulting pearl being placed
	 * in the inventory of the killing player.
	 * <p>
	 * This method will return null if the killing player is offline.
	 * <p>
	 * For instances when the killer could be offline, 
	 * {@link PearlAccess#exilePlayer(UUID, UUID, Location)} or
	 * {@link PearlAccess#exilePlayer(UUID, UUID, PearlHolder)} should be used.
	 * @see PearlAccess#exilePlayer(UUID, UUID, PearlHolder)
	 * @param exiledId The exiled player Id
	 * @param killer The killing player
	 * @return The new {@link ExilePearl} instance if the operation succeeds, 
	 * otherwise null
	 */
	ExilePearl exilePlayer(UUID exiledId, Player killer);
	
	/**
	 * Gets a pearl instance by player name
	 * @param name The player name to search for
	 * @return The {@link ExilePearl} instance if found, otherwise null
	 */
	ExilePearl getPearl(String name);
	
	/**
	 * Gets a pearl instance by player UUID
	 * @param uid The player UUID to search for
	 * @return The {@link ExilePearl} instance if found, otherwise null
	 */
	ExilePearl getPearl(UUID uid);
	
	/**
	 * Gets an immutable collection of all {@link ExilePearl} instances.
	 * This collection can't be modified.
	 * @return A collection of all {@link ExilePearl} instances
	 */
	Collection<ExilePearl> getPearls();

	/**
	 * Checks whether a player is exiled
	 * @param player The player to check
	 * @return true if the player is exiled
	 */
	boolean isPlayerExiled(Player player);
	
	/**
	 * Checks whether a player is exiled
	 * @param uid The player UUID to check
	 * @return true if the player is exiled
	 */
	boolean isPlayerExiled(UUID uid);
	
	/**
	 * Attempts to get an {@link ExilePearl} instance from the item stack of
	 * of the pearl.
	 * <p>
	 * This will only return a non-null value if the item stack parameter is
	 * a valid ExilePearl item.
	 * @param is The item stack to check
	 * @return The {@link ExilePearl} instance if found, otherwise null
	 */
	ExilePearl getPearlFromItemStack(ItemStack is);
	
	/**
	 * Frees an exile pearl
	 * <p>
	 * It's possible for the free operation to fail if the
	 * {@link com.devotedmc.ExilePearl.event.PlayerFreedEvent PlayerFreedEvent} is canceled.
	 * @param pearl The pearl instance to free
	 * @param reason The reason that the pearl is being freed
	 * @return true if the pearl is freed, otherwise false
	 */
	boolean freePearl(ExilePearl pearl, PearlFreeReason reason);
	
	/**
	 * Summons a prison pearl
	 * <p>
	 * It's possible for the summon operation to fail if the
	 * {@link com.devotedmc.ExilePearl.event.PlayerSummonEvent PlayerSummonEvent} is cancelled.
	 * Or if the target player is dead or already summoned (or offline)
	 * @param pearl The pearl to summon
	 * @param summoner The player summoning the prisoner
	 * @return true if the player is summoned, otherwise false
	 */
	boolean summonPearl(ExilePearl pearl, Player summoner);
}
