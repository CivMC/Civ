package com.devotedmc.ExilePearl.holder;

import org.bukkit.Location;

import com.devotedmc.ExilePearl.ExilePearl;

/**
 * Interface for an object that can hold or contain an exile pearl
 * @author Gordon
 *
 */
public interface PearlHolder {

	/**
	 * Gets the holder name
	 * @return The holder name
	 */
	public String getName();
	
	/**
	 * Gets the holder location
	 * @return The holder location
	 */
	public Location getLocation();
	
	/**
	 * Validate that the given pearl still exists in this holder instance
	 * @param pearl The exile pearl
	 * @return the validation result
	 */
	public HolderVerifyResult validate(final ExilePearl pearl);
	
	/**
	 * Gets whether the holder is a block
	 * @return true if the holder is a block
	 */
	public boolean isBlock();
}
