package vg.civcraft.mc.civmodcore.utilities.cooldowns;

/**
 * Allows keeping track of one specific cooldown for any amount of objects of the same type. Common examples would be
 * cooldown for players by their UUIDs
 *
 * @param <E>
 *            Object that cooldowns are assigned to
 * @author Maxopoly
 */
public interface ICoolDownHandler<E> {

	/**
	 * Sets the cooldown to it's maximum duration for the given object
	 *
	 * @param e
	 *            Object to set cooldown for
	 */
	void putOnCoolDown(E e);

	/**
	 * Checks whether the given object is currently on cooldown in this instance
	 *
	 * @param e
	 *            Object to check
	 * @return True if the object is on cooldown, false if not
	 */
	boolean onCoolDown(E e);

	/**
	 * Gets the remaining cooldown of the given item. The actual meaning of this number depends on the implementation,
	 * but may never be less than 0 and never higher than the predefined maximum cooldown
	 *
	 * @param e
	 *            Object to get cooldown for
	 * @return The cooldown left for the given object or 0 if none exists
	 */
	long getRemainingCoolDown(E e);

	/**
	 * Gives the total cooldown set for this instance. Again the actual meaning of this value depends on the
	 * implementation
	 *
	 * @return Maximum cooldown in this instance
	 */
	long getTotalCoolDown();

	/**
	 * Removed the cooldown for the given object
	 * 
	 * @param e 
	 *            Object to check
	 */
	void removeCooldown(E e);

}
