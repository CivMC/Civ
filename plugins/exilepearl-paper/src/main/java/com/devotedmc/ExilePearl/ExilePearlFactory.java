package com.devotedmc.ExilePearl;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ExilePearlFactory {

	/**
	 * Creates an exile pearl instance from a location
	 * @param uid The prisoner UUID
	 * @param location The location of the pearl
	 * @param strength The pearl strength
	 * @return The new exile pearl instance
	 */
	ExilePearl createExilePearl(UUID uid, String killedBy, Location location, int strength);
	
	/**
	 * Creates an exile pearl instance from a player holder
	 * @param uid The prisoner UUID
	 * @param killedBy The killing player
	 * @param strength The pearl strength
	 * @return The new exile pearl instance
	 */
	ExilePearl createExilePearl(UUID uid, Player killedBy, int strength);
}
