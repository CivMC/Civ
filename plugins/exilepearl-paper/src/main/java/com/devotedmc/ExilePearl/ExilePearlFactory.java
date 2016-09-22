package com.devotedmc.ExilePearl;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ExilePearlFactory {

	/**
	 * Creates an exile pearl instance from a location
	 * @param uid The prisoner UUID
	 * @param location The location of the pearl
	 * @return The new exile pearl instance
	 */
	ExilePearl createExilePearl(UUID uid, Location location);
	
	/**
	 * Creates an exile pearl instance from a player holder
	 * @param uid The prisoner UUID
	 * @param player The player holding the pearl
	 * @return The new exile pearl instance
	 */
	ExilePearl createExilePearl(UUID uid, Player player);
}
