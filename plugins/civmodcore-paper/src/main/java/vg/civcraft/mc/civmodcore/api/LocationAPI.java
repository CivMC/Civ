package vg.civcraft.mc.civmodcore.api;

import org.bukkit.Location;

/**
 * Class of utility functions for Locations.
 */
public final class LocationAPI {

	/**
	 * Determines whether a location is valid and safe to use.
	 *
	 * @param location The location to check.
	 * @return Returns true if the location exists, is valid, and safe to use.
	 */
	public static boolean isValidLocation(Location location) {
		if (location == null) {
			return false;
		}
		if (!location.isWorldLoaded()) {
			return false;
		}
		return true;
	}

}
