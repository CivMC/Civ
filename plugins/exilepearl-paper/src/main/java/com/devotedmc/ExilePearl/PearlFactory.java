package com.devotedmc.ExilePearl;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface PearlFactory {

	/**
	 * Creates an exile pearl instance from a location
	 * @param uid The prisoner UUID
	 * @param killedBy The killing player UUID
	 * @param location The location of the pearl
	 * @param health The pearl health
	 * @return The new exile pearl instance
	 */
	ExilePearl createExilePearl(UUID uid, UUID killedBy, Location location, double health);
	
	/**
	 * Creates an exile pearl instance from a player holder
	 * @param uid The prisoner UUID
	 * @param killedBy The killing player
	 * @param health The pearl health
	 * @return The new exile pearl instance
	 */
	ExilePearl createExilePearl(UUID uid, Player killedBy, double health);
}
