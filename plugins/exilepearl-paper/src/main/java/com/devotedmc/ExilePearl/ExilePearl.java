package com.devotedmc.ExilePearl;

import java.util.Date;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.devotedmc.ExilePearl.broadcast.BroadcastListener;
import com.devotedmc.ExilePearl.holder.PearlHolder;

/**
 * Interface for a player who is imprisoned in an exile pearl
 * @author Gordon
 */
public interface ExilePearl {

	/**
	 * Gets the pearl item name
	 * @return The item name
	 */
	String getItemName();

	/**
	 * Gets the exiled player ID
	 * @return The player ID
	 */
	UUID getPlayerId();

	/**
	 * Gets the unique pearl ID
	 * @return The pearl ID
	 */
	int getPearlId();

	/**
	 * Gets the exiled player instance
	 * @return The exiled player 
	 */
	Player getPlayer();

	/**
	 * Gets the pearl type
	 * @return The pearl type
	 */
	PearlType getPearlType();

	/**
	 * Sets the pearl type
	 * @param type The pearl type
	 */
	void setPearlType(PearlType type);

	/**
	 * Gets when the player was pearled
	 * @return The time the player was pearled
	 */
	Date getPearledOn();

	/**
	 * Sets when the player was pearled
	 * @param pearledOn The time the player was pearled
	 */
	void setPearledOn(Date pearledOn);

	/**
	 * Gets the exiled player's name
	 * @return The exiled player's name
	 */
	String getPlayerName();

	/**
	 * Gets the pearl holder
	 * @return The pearl holder
	 */
	PearlHolder getHolder();

	/**
	 * Sets the pearl holder to a player
	 * @param player The new pearl holder
	 */
	void setHolder(Player player);

	/**
	 * Sets the pearl holder to a block
	 * @param block The new pearl block
	 */
	void setHolder(Block block);

	/**
	 * Sets the pearl holder to a location
	 * @param item The new pearl item
	 */
	void setHolder(Item item);

	/**
	 * Sets the pearl holder to an entity
	 * @param entity The new pearl entity
	 */
	void setHolder(Entity entity);

    /**
     * Gets the pearl health value
     * @return The health value
     */
    int getHealth();

    /**
     * Gets the pearl health percent value
     * @return The health percent value
     */
    Integer getHealthPercent();
    
    /**
     * Sets the pearl health value
     * @param health The health value
     */
    void setHealth(int health);
    
    /**
     * Gets the location of the pearl
     * @return The location of the pearl
     */
	Location getLocation();

	/**
	 * Sets the killer ID
	 * @param killerId The killer ID
	 */
	void setKillerId(UUID killerId);

	/**
	 * Gets the ID of the killing player
	 * @return The ID of the killing player
	 */
	UUID getKillerId();

	/**
	 * Gets the name of the killing player
	 * @return The name of the killing player
	 */
	String getKillerName();

	/**
	 * Gets the string describing the pearl current location
	 * @return The description of the current location
	 */
	String getLocationDescription();

	/**
	 * Gets whether the player was freed offline
	 * @return true if the player was freed offline
	 */
	boolean getFreedOffline();

	/**
	 * Gets whether the player was freed offline
	 * @param freedOffline The freed offline value
	 */
	void setFreedOffline(boolean freedOffline);

	/**
	 * Creates an item stack for the pearl
	 * @return The new item stack
	 */
	ItemStack createItemStack();

	/**
	 * Verifies the pearl location
	 * @return true if the location is verified
	 */
	boolean verifyLocation();

	/**
	 * Validates that an item stack is the exile pearl
	 * @param is The item stack
	 * @return true if validation passes
	 */
	boolean validateItemStack(ItemStack is);

	/**
	 * Enables storage updates when writing values
	 */
	void enableStorage();

	/**
	 * Broadcasts the pearl location to all the listeners
	 */
	void performBroadcast();

	/**
	 * Adds a broadcast target
	 * @param bcast The broadcast target
	 */
	void addBroadcastListener(BroadcastListener bcast);

	/**
	 * Removes a broadcast target
	 * @param bcast The broadcast target
	 */
	void removeBroadcastListener(Object bcast);

	/**
	 * Checks if the pearl is broadcasting to an object
	 * @param o The object to check
	 * @return true if it is broadcasting
	 */
	boolean isBroadcastingTo(Object o);

	/**
	 * Gets last recorded appearance online for the player
	 * held by this pearl
	 * 
	 * @return last online Date
	 */
	Date getLastOnline();

	/**
	 * Resets the recorded appearance online for a player
	 * held by this pearl
	 * 
	 * @param online The new latest Seen Date.
	 */
	void setLastOnline(Date online);

	/**
	 * Gets whether or not the pearled player is summoned
	 * This is always false for exiled players, summoning only works for upgraded PrisonPearls
	 * @return true if the player is summoned
	 */
	boolean isSummoned();

	/**
	 * Sets the summoned state of a player
	 * Can only be true if pearl is of prison type
	 * @param summoned The summoned value
	 */
	void setSummoned(boolean summoned);

	/**
	 * Gets the location where player should be returned from summon
	 * @return The location
	 */
	Location getReturnLocation();

	/**
	 * Sets the location where a player should be returned from summon
	 * @param loc The location
	 */
	void setReturnLocation(Location loc);
	
	/**
	 * Gets the multiplier to apply to the amount of repair materials needed to repair the pearl 
	 * based on how long the player has been imprisoned
	 * @return Repair cost multiplier
	 */
	double getLongTimeMultiplier();
}
