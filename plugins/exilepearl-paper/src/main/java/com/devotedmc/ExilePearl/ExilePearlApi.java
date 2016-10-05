package com.devotedmc.ExilePearl;

import java.util.UUID;

import org.bukkit.plugin.Plugin;

/**
 * External API for ExilePearl
 * @author Gordon
 *
 */
public interface ExilePearlApi extends PearlAccess, PearlLogger, PlayerProvider {
	
	/**
	 * Gets the plugin name
	 * @return The plugin name
	 */
	String getPluginName();
	
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
	 * Gets the exile pearl configuration
	 * @return The exile pearl configuration
	 */
	PearlConfig getPearlConfig();
	
	/**
	 * Gets the pearl lore provider
	 * @return The lore provider
	 */
	PearlLoreProvider getLoreProvider();
	
	/**
	 * Gets the plugin instance
	 * @return The plugin instance
	 */
	Plugin getPlugin();
}
