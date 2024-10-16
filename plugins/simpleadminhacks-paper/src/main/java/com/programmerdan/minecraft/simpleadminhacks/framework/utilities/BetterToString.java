package com.programmerdan.minecraft.simpleadminhacks.framework.utilities;

import org.bukkit.Location;
import org.bukkit.World;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

public final class BetterToString {

	public static String location(final Location location) {
		if (location == null) {
			return "<null>";
		}
		final World world = WorldUtils.getLocationWorld(location);
		return (world == null ? "<null world>" : world.getName())
				+ ":"
				+ "x=" + location.getX()
				+ "y=" + location.getY()
				+ "z=" + location.getZ()
				+ ";"
				+ "pitch=" + location.getPitch()
				+ "yaw=" + location.getYaw();
	}

}
