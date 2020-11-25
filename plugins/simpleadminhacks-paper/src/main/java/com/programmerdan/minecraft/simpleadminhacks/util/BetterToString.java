package com.programmerdan.minecraft.simpleadminhacks.util;

import org.bukkit.Location;
import org.bukkit.World;
import vg.civcraft.mc.civmodcore.api.LocationAPI;

public final class BetterToString {

	public static String location(final Location location) {
		if (location == null) {
			return "<null>";
		}
		final World world = LocationAPI.getLocationWorld(location);
		return String.format("%s:x=%s,y=%s,z=%s;pitch=%s,yaw=%s",
				world == null ? "null" : world.getName(),
				location.getX(), location.getY(), location.getZ(),
				location.getPitch(), location.getYaw());
	}

}
