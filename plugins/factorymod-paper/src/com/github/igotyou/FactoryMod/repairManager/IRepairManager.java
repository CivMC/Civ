package com.github.igotyou.FactoryMod.repairManager;

/**
 * Used to manager the health of a factory and to handle repairs for it.
 *
 */
public interface IRepairManager {
	/**
	 * Sets the health to 0 and makes the factory unusable
	 */
	public void breakIt();

	/**
	 * @return How much factory the factory represented currently has
	 */
	public String getHealth();

	/**
	 * @return Whether the factory represented is currently at full health
	 */
	public boolean atFullHealth();

	/**
	 * Repairs the factory by the given amount, which means the health is
	 * increased
	 * 
	 * @param amount
	 *            Health to restore
	 */
	public void repair(int amount);

	/**
	 * @return Whether the factory is in disrepair currently
	 */
	public boolean inDisrepair();

}
