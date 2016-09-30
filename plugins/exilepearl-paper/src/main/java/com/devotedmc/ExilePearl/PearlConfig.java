package com.devotedmc.ExilePearl;

public interface PearlConfig {
	
	/**
	 * Gets the database host
	 * @return the database host
	 */
	String getDbHost();

	/**
	 * Gets the database user name
	 * @return the database user name
	 */
	String getDbUsername();
	
	/**
	 * Gets the database password
	 * @return the database password
	 */
	String getDbPassword();
	
	/**
	 * Gets the database name
	 * @return the database name
	 */
	String getDbName();
	
	/**
	 * Gets the database port
	 * @return the database port
	 */
	int getDbPort();
	
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
	 * Gets the pearl health material
	 * @return the pearl health material
	 */
	int getPearlHealthMaterial();
	
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
	
	int getDamageLogMin();
	
	int getDamagelogTicks();
	
	
	// Exile Rules

	int getRulePearlRadius();
	
	boolean setRulePearlRadius(Integer value);
	
	boolean getRuleCanDamageReinforcement();
	
	boolean setRuleCanDamageReinforcement(Boolean value);
	
	boolean getRuleCanDamageBastion();
	
	boolean setRuleCanDamageBastion(Boolean value);

	boolean getRuleCanEnterBastion();
	
	boolean setRuleCanEnterBastion(Boolean value);
	
	boolean getRuleCanThrowEnderPearl();
	
	boolean setRuleCanThrowEnderPearl(Boolean value);

	boolean getRuleCanChatLocal();
	
	boolean setRuleCanChatLocal(Boolean value);

	boolean getRuleCanPvp();
	
	boolean setRuleCanPvp(Boolean value);

	boolean getRuleCanIgnite();
	
	boolean setRuleCanIgnite(Boolean value);

	boolean getRuleCanUseBucket();
	
	boolean setRuleCanUseBucket(Boolean value);

	boolean getRuleCanPlaceWater();
	
	boolean setRuleCanPlaceWater(Boolean value);

	boolean getRuleCanPlaceLava();
	
	boolean setRuleCanPlaceLava(Boolean value);

	boolean getRuleCanUsePotions();
	
	boolean setRuleCanUsePotions(Boolean value);

	boolean getRuleCanUseBed();
	
	boolean setRuleCanUseBed(Boolean value);

	boolean getRuleCanSuicide();
	
	boolean setRuleCanSuicide(Boolean value);

	boolean getRuleCanPlaceSnitch();
	
	boolean setRuleCanPlaceSnitch(Boolean value);

	boolean getRuleCanMine();
	
	boolean setRuleCanMine(Boolean value);

	boolean getRuleCanBrew();
	
	boolean setRuleCanBrew(Boolean value);

	boolean getRuleCanEnchant();
	
	boolean setRuleCanEnchant(Boolean value);
}
