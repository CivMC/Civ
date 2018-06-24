package com.devotedmc.ExilePearl;

import com.devotedmc.ExilePearl.command.PearlCommand;
import com.devotedmc.ExilePearl.config.PearlConfig;
import com.devotedmc.ExilePearl.util.BastionWrapper;
import com.devotedmc.ExilePearl.util.Clock;
import java.util.List;
import java.util.UUID;
import net.minelink.ctplus.compat.api.NpcIdentity;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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
	LoreProvider getLoreProvider();

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
	 * Gets the damage logger
	 * @return The damage logger
	 */
	DamageLogger getDamageLogger();

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
	 * Gets a player as a tagged NPC entity.
	 * <p>
	 * This will return null for a normal player.
	 * @param player The player being referenced
	 * @return The NPC entity instance
	 */
	NpcIdentity getPlayerAsTaggedNpc(Player player);

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
	 * Gets whether BanStick hooks are enabled
	 * @return True if it is enabled
	 */
	boolean isBanStickEnabled();

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
	 * Gets whether MythicMobs hooks are enabled
	 * @return True if it is enabled
	 */
	boolean isMythicMobsEnabled();

	/**
	 * Checks if a location is inside the world border
	 * @param location the location to check
	 * @return true if the location is inside the border
	 */
	boolean isLocationInsideBorder(Location location);

	/**
	 * Gets whether a player is in a non-permission bastion
	 * @param player The player to check
	 * @return true if the player is inside a non-permission bastion
	 */
	boolean isPlayerInUnpermittedBastion(Player player);

	/**
	 * Gets bastions the player is in, that they shouldn't be in.
	 * @param player The player to check
	 * @return list of bastions the player is inside that they shouldn't be
	 */
	List<BastionWrapper> getPlayerInUnpermittedBastion(Player player);

	/**
	 * Gets whether an entity is a Mythic Mob entity
	 * @param entity the entity to check
	 * @return true if it is a mythic mob
	 */
	boolean isMythicMob(Entity entity);

	/**
	 * Gets the clock instance
	 * @return the clock instance
	 */
	Clock getClock();

	/**
	 * Gets the Brew handler for brew checking, if any.
	 *
	 * @return a Brew Handler appropriate for the server.
	 */
	BrewHandler getBrewHandler();
}
