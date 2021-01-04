package vg.civcraft.mc.civmodcore.api;

import org.bukkit.Location;
import org.bukkit.World;
import vg.civcraft.mc.civmodcore.util.NullCoalescing;

/**
 * Class of utility functions for Locations.
 *
 * @deprecated Use {@link vg.civcraft.mc.civmodcore.world.WorldUtils} instead.
 */
@Deprecated
public final class LocationAPI {

	/**
	 * Retrieves the world from a location.
	 *
	 * @param location The location to retrieve the world from.
	 * @return Returns the world if loaded, or null.
	 *
	 * @deprecated Use {@link vg.civcraft.mc.civmodcore.world.WorldUtils#getLocationWorld(Location)} instead.
	 */
	@Deprecated
	public static World getLocationWorld(Location location) {
		if (location == null) {
			return null;
		}
		try {
			return location.getWorld();
		}
		catch (IllegalArgumentException ignored) { // Will be thrown if the world is not loaded
			return null;
		}
	}

	/**
	 * Determines whether a location is valid and safe to use.
	 *
	 * @param location The location to check.
	 * @return Returns true if the location exists, is valid, and safe to use.
	 *
	 * @deprecated Use {@link vg.civcraft.mc.civmodcore.world.WorldUtils#isValidLocation(Location)} instead.
	 */
	@Deprecated
	public static boolean isValidLocation(Location location) {
		if (location == null) {
			return false;
		}
		if (!location.isWorldLoaded()) {
			return false;
		}
		return true;
	}

	/**
	 * Converts a location into a block location. (Yaw and Pitch values are lost)
	 *
	 * @param location The location to convert.
	 * @return Returns a block location, or null if the given location was null.
	 *
	 * @deprecated Use {@link vg.civcraft.mc.civmodcore.world.WorldUtils#getBlockLocation(Location)} instead.
	 */
	@Deprecated
	public static Location getBlockLocation(Location location) {
		if (location == null) {
			return null;
		}
		return new Location(getLocationWorld(location),
				location.getBlockX(),
				location.getBlockY(),
				location.getBlockZ());
	}

	/**
	 * Converts a location into a block's mid point. (Yaw and Pitch values are lost)
	 *
	 * @param location The location to convert.
	 * @return Returns a block's mid point, or null if the given location was null.
	 *
	 * @deprecated Use {@link vg.civcraft.mc.civmodcore.world.WorldUtils#getMidBlockLocation(Location)} instead.
	 */
	@Deprecated
	public static Location getMidBlockLocation(Location location) {
		if (location == null) {
			return null;
		}
		return getBlockLocation(location).add(0.5d, 0.5d, 0.5d);
	}

	/**
	 * Determines whether two locations share the same world.
	 *
	 * @param former The first location.
	 * @param latter The second location.
	 * @return Returns true if the two locations are not null and share the same world.
	 *
	 * @deprecated Use {@link vg.civcraft.mc.civmodcore.world.WorldUtils#doLocationsHaveSameWorld(Location, Location)}
	 *     instead.
	 */
	@Deprecated
	public static boolean areLocationsSameWorld(Location former, Location latter) {
		if (former == null || latter == null) {
			return false;
		}
		return NullCoalescing.equalsNotNull(getLocationWorld(former), getLocationWorld(latter));
	}

	/**
	 * Returns the largest axis distance.
	 *
	 * @param latter The first location.
	 * @param former The second location.
	 * @param consider2D Whether only the X and Z axis should be considered. (true if yes)
	 * @return Returns the largest axis distance, or -1 if there's a problem,
	 *     like the two locations being in two different worlds.
	 *
	 * @deprecated Use {@link vg.civcraft.mc.civmodcore.world.WorldUtils#blockDistance(Location, Location, boolean)}
	 *     instead.
	 */
	@Deprecated
	public static int blockDistance(final Location former, final Location latter, final boolean consider2D) {
		if (!LocationAPI.areLocationsSameWorld(former, latter)) {
			return -1;
		}
		final int x = Math.abs(former.getBlockX() - latter.getBlockX());
		final int z = Math.abs(former.getBlockZ() - latter.getBlockZ());
		if (consider2D) {
			return Math.max(x, z);
		}
		else {
			final int y = Math.abs(former.getBlockY() - latter.getBlockY());
			return Math.max(x, Math.max(y, z));
		}
	}

	/**
	 * Checks whether a location's Y coordinate is a valid block location.
	 *
	 * @param location The location to check.
	 * @return Returns true if the Y coordinate is a valid block location. (False if given location is null!)
	 *
	 * @deprecated Use {@link vg.civcraft.mc.civmodcore.world.WorldUtils#isWithinBounds(Location)} instead.
	 */
	@Deprecated
	public static boolean isWithinBounds(final Location location) {
		if (location == null) {
			return false;
		}
		final double y = location.getY();
		if (y < 0 || y >= 256) {
			return false;
		}
		return true;
	}

}
