package com.devotedmc.ExilePearl;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import com.devotedmc.ExilePearl.command.PearlCommand;
import com.devotedmc.ExilePearl.config.PearlConfig;

/**
 * The API for the ExilePearl plugin.
 * @author Gordon
 *
 */
public interface ExilePearlApi extends Plugin, PearlAccess, PearlLogger, PlayerProvider {
	
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
	 * Gets the storage provider
	 * @return The storage provider
	 */
	StorageProvider getStorageProvider();
	
	/**
	 * Gets the suicide handler
	 * @return The suicide handler
	 */
	SuicideHandler getSuicideHandler();
	
	/**
	 * Gets the pearl manager
	 * @return The pearl manager
	 */
	PearlManager getPearlManager();
	
	/**
	 * Gets the auto-help command
	 * @return The auto-help
	 */
	PearlCommand getAutoHelp();
	
	/**
	 * Gets whether a player is combat tagged
	 * @param uid The player UUID
	 * @return true if the player is tagged
	 */
	boolean isPlayerTagged(UUID uid);
	
	/**
	 * Gets whether NameLayer hooks are enabled
	 * @return True if it is enabled
	 */
	boolean isNameLayerEnabled();
	
	/**
	 * Gets whether Citadel hooks are enabled
	 * @return True if it is enabled
	 */
	boolean isCitadelEnabled();
	
	/**
	 * Gets whether CivChat hooks are enabled
	 * @return True if it is enabled
	 */
	boolean isCivChatEnabled();
	
	/**
	 * Gets whether Bastion hooks are enabled
	 * @return True if it is enabled
	 */
	boolean isBastionEnabled();
	
	/**
	 * Gets whether JukeAlert hooks are enabled
	 * @return True if it is enabled
	 */
	boolean isJukeAlertEnabled();
	
	/**
	 * Gets whether RandomSpawn hooks are enabled
	 * @return True if it is enabled
	 */
	boolean isRandomSpawnEnabled();
	
	/**
	 * Gets whether WorldBorder hooks are enabled
	 * @return True if it is enabled
	 */
	boolean isWorldBorderEnabled();
	
	/**
	 * Gets whether CombatTag hooks are enabled
	 * @return True if it is enabled
	 */
	boolean isCombatTagEnabled();
	
	/**
	 * Checks if a location is inside the world border
	 * @param location the location to check
	 * @return true if the location is inside the border
	 */
	boolean isLocationInsideBorder(Location location);
}
