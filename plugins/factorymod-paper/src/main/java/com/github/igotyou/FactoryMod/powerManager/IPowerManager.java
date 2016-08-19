package com.github.igotyou.FactoryMod.powerManager;

/**
 * Manager to handle power availability and power consumption for a specific
 * factory
 *
 */
public interface IPowerManager {
	/**
	 * Consumes one unit of power, what that means is up to the concrete
	 * implementation
	 */
	public void consumePower();

	/**
	 * @return Whether power for at least one further tick cycle is available
	 */
	public boolean powerAvailable();

	/**
	 * @return How often power should be consumed when running the factory this
	 *         manager is associated with, measure in ticks
	 */
	public int getPowerConsumptionIntervall();

	/**
	 * @return Internal counter to count up until power needs to be consumed
	 *         while a factory is running. Should be in ticks
	 */
	public int getPowerCounter();

	/**
	 * Increases the internal power counter by the given amount
	 * 
	 * @param amount
	 *            Amount to increase by
	 */
	public void increasePowerCounter(int amount);

	/**
	 * Sets the internal power counter to the given value. Use this method with
	 * great care and try to use increasePowerCounter() instead if possible
	 * 
	 * @param value
	 *            New power counter value
	 */
	public void setPowerCounter(int value);

}
