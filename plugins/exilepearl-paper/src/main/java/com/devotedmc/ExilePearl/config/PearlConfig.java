package com.devotedmc.ExilePearl.config;

import java.util.List;
import java.util.Set;

import com.devotedmc.ExilePearl.ExileRule;
import com.devotedmc.ExilePearl.RepairMaterial;
import com.devotedmc.ExilePearl.storage.StorageType;

public interface PearlConfig extends MySqlConfig, DocumentConfig {
	
	/**
	 * Gets the storage type to use
	 * @return The storage type
	 */
	StorageType getStorageType();
	
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
	 * Gets how much to damage the players when they are inside a bastion field
	 * @return The damage amount
	 */
	double getBastionDamage();
	
	/**
	 * Gets the pearl repair materials
	 * @return The pearl repair materials
	 */
	Set<RepairMaterial> getRepairMaterials();
	
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
	
	int getDamageLogMin();
	
	int getDamagelogTicks();

	int getRulePearlRadius();
	
	void setRulePearlRadius(int value);
	
	boolean canPerform(ExileRule rule);
	
	void setRule(ExileRule rule, boolean value);
	
	boolean getUseHelpItem();
	
	String getHelpItemName();
	
	List<String> getHelpItemText();
}
