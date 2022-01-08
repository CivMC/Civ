package com.devotedmc.ExilePearl.config;

import com.devotedmc.ExilePearl.ExileRule;
import com.devotedmc.ExilePearl.PearlType;
import com.devotedmc.ExilePearl.RepairMaterial;
import com.devotedmc.ExilePearl.storage.StorageType;
import java.util.List;
import java.util.Set;
import org.bukkit.World;

public interface PearlConfig extends MySqlConfig, DocumentConfig {

	/**
	 * Gets the storage type to use
	 * @return The storage type
	 */
	StorageType getStorageType();

	/**
	 * Gets a set of worlds that pearls can not be stored in
	 * @return a set of names of worlds pearls can't be stored in
	 */
	Set<String> getDisallowedWorlds();

	/**
	 * Gets the name of the interval humans measure pearl decay in
	 */
	String getPearlHealthDecayHumanInterval();

	/**
	 * Gets the length of the interval humans measure pearl decay in (minutes)
	 */
	int getPearlHealthDecayHumanIntervalMin();

	/**
	 * Gets the pearl decay minute interval
	 * @return the pearl decay minute interval
	 */
	int getPearlHealthDecayIntervalMin();

	/**
	 * Gets the pearl decay amount
	 * @return the pearl decay amount
	 */
	int getPearlHealthDecayAmount();

	/**
	 * Gets the pearl health decay shutoff timeout (time since last login
	 *  that decay is no longer applied)
	 * @return the # of minutes absent at which decay is skipped.
	 */
	int getPearlHealthDecayTimeout();

	/**
	 * Gets the pearl health start value
	 * @return the pearl health start value
	 */
	int getPearlHealthStartValue();

	/**
	 * Gets the pearl health max value
	 * @return the pearl max start value
	 */
	int getPearlHealthMaxValue();

	/**
	 * Gets whether pearls outside world border should be freed
	 * @return the pearl max start value
	 */
	boolean getShouldAutoFreeWorldBorder();

	/**
	 * Gets whether the pearl must be in the hot bar
	 * @return the pearl max start value
	 */
	boolean getMustPrisonPearlHotBar();

	/**
	 * Gets whether pearls can be freed by throwing them
	 * @return true if they can be freed by throwing
	 */
	boolean getFreeByThrowing();

	/**
	 * Gets whether freeing a prison pearl should teleport the player
	 * @return true if they should be teleported
	 */
	boolean getShouldFreeTeleport();

	/**
	 * Gets whether killing exiles should announce their location
	 * @return true if it should be announced
	 */
	boolean getShouldAnnounceExileLocation();

	/**
	 * Gets how much to damage the players when they are inside a bastion field
	 * @return The damage amount
	 */
	double getBastionDamage();

	/**
	 * Gets the pearl repair materials
	 * @param type The type for repairs
	 * @return The pearl repair materials
	 */
	Set<RepairMaterial> getRepairMaterials(PearlType type);

	/**
	 * Gets the names of animals that are protected
	 * @return The protected animal names
	 */
	List<String> getProtectedAnimals();

	/**
	 * Gets the suicide timeout in seconds
	 * @return The suicide timeout
	 */
	int getSuicideTimeoutSeconds();

	/**
	 * Gets whether the damage log is enabled
	 * @return true if the damage log is enabled
	 */
	boolean getDamageLogEnabled();

	/**
	 * Gets the damage log algorithm type
	 * @return The algorithm type
	 */
	int getDamageLogAlgorithm();

	/**
	 * Gets the damage log tick interval
	 * @return The damage log tick interval
	 */
	int getDamageLogInterval();

	/**
	 * Gets the damage log decay amount
	 * @return The decay amount
	 */
	double getDamageLogDecayAmount();

	/**
	 * Gets the max tracked damage
	 * @return The max tracked damage
	 */
	double getDamageLogMaxDamage();

	/**
	 * Gets the potion damage amount
	 * @return The potion damage amount
	 */
	double getDamageLogPotionDamage();

	int getRulePearlRadius();

	void setRulePearlRadius(int value);

	boolean canPerform(ExileRule rule);

	void setRule(ExileRule rule, boolean value);

	boolean getUseHelpItem();

	String getHelpItemName();

	List<String> getHelpItemText();

	World getPrisonWorld();

	World getMainWorld();

	Set<RepairMaterial> getUpgradeMaterials();

	boolean allowPearlStealing();

	boolean allowSummoning();

	/**
	 * @return Maximum ban stick alts allowed to be pearled
	 */
	int maxAltsPearled();

	/**
	 * @return Message displayed when attempting to login when having to many pearled alts
	 */
	String altBanMessage();
	
	int pearlCostMultiplicationTimerDays();

	/**
	 * Gets whether people should be allowed to deploy an elytra with a pearl in their inventory
	 * Also determines if they should be able to pick up pearl items while gliding
	 * @return true if they should be able to fly and pick up exile pearls while gliding
	 */
	boolean canElytraWithPearls();
}
