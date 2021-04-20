package vg.civcraft.mc.civmodcore.util;

/**
 * This exists to try and standardise initialising and resetting objects.
 */
public interface Resettable {

	/**
	 * Initialises this object, which is irrelevant to construction.
	 */
	void init();

	/**
	 * Resets this object, which is irrelevant to finalisation / closure.
	 */
	void reset();

}
