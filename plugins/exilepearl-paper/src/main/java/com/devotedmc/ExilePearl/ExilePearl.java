package com.devotedmc.ExilePearl;

import java.util.Date;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

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
	UUID getUniqueId();

	/**
	 * Gets the exiled player instance
	 * @return The exiled player 
	 */
	PearlPlayer getPlayer();

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
	 * @param holder The new pearl holder
	 */
	void setHolder(PearlPlayer p);

	/**
	 * Sets the pearl holder to a block
	 * @param holder The new pearl block
	 */
	void setHolder(Block b);

	/**
	 * Sets the pearl holder to a location
	 * @param holder The new pearl location
	 */
	void setHolder(Location l);

    /**
     * Gets the pearl health from 0 to 100
     * @return The health value
     */
    double getHealth();
    
    /**
     * Sets the pearl health from 0 to 100
     * @param health The health value
     */
    void setHealth(double health);
    
    /**
     * Gets the location of the pearl
     * @return The location of the pearl
     */
	Location getLocation();
	
	/**
	 * Gets the name of the killing player
	 * @return The name of the killing player
	 */
	String getKilledByName();

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
	 * @return true if the player was freed offline
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
}
