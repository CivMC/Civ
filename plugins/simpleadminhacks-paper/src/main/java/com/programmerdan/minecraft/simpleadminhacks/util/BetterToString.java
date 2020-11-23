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
		return (world == null ? "null" : world.getName()) + ":" +
				"x=" + location.getX() + "," +
				"y=" + location.getY() + "," +
				"z=" + location.getZ() + ";" +
				"pitch=" + location.getPitch() + "," +
				"yaw=" + location.getYaw();
	}

}
