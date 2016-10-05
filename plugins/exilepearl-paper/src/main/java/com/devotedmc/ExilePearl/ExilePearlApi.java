package com.devotedmc.ExilePearl;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * External API for ExilePearl
 * @author Gordon
 *
 */
public interface ExilePearlApi extends PearlAccess, PearlLogger {
	
	/**
	 * Gets a player instance by UUID
	 * @param uniqueId The player UUID
	 * @return The PearlPlayer instance
	 */
	PearlPlayer getPearlPlayer(final UUID uid);
	
	/**
	 * Gets a player instance by name
	 * @param uniqueId The player name
	 * @return The PearlPlayer instance
	 */
	PearlPlayer getPearlPlayer(final String name);
	
	/**
	 * Gets a player instance by player instance
	 * @param uniqueId The player instance
	 * @return The PearlPlayer instance
	 */
	PearlPlayer getPearlPlayer(final Player player);
	
	/**
	 * Gets the exile pearl configuration
	 * @return The exile pearl configuration
	 */
	PearlConfig getPearlConfig();
	
	/**
	 * Gets the pearl lore generator
	 * @return The lore generator
	 */
	PearlLoreProvider getLoreGenerator();
	
	/**
	 * Gets the plugin scheduler
	 * @return The plugin scheduler
	 */
	BukkitScheduler getScheduler();
	
	/**
	 * Gets the java plugin instance
	 * @return The plugin plugin instance
	 */
	JavaPlugin getPlugin();
}
