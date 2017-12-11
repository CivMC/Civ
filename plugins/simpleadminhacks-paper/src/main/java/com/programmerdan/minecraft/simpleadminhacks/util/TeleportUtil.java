package com.programmerdan.minecraft.simpleadminhacks.util;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;

public class TeleportUtil {

	private static boolean checkForTeleportSpace(Location loc) {
		final Block block = loc.getBlock();
		return !block.getType().isSolid() && !block.getRelative(BlockFace.UP).getType().isSolid() &&
				!block.getRelative(BlockFace.UP).getRelative(BlockFace.UP).getType().isSolid();
	}
	
	private static Location getTeleportLocation(Player player, Location location) {
		if(location.getY() >= 1) {
			final Block block = location.getBlock();
			if(block.getRelative(BlockFace.DOWN).getType().isSolid() && checkForTeleportSpace(location)) {
				return location;
			} else {
				return getTeleportLocation(player, location.add(0, -1, 0));
			}
		} else {
			return null;
		}
	}
	
	public static boolean tryToTeleportVertically(Player player, Location location, String reason) {
		Location loc = location.clone();
		loc.setX(Math.floor(loc.getX()) + 0.500000D);
		loc.setY(Math.floor(loc.getY()) + 0.02D);
		loc.setZ(Math.floor(loc.getZ()) + 0.500000D);
		final Location baseLoc = loc.clone();
		final World world = baseLoc.getWorld();
		boolean teleport = checkForTeleportSpace(loc);
		if(!teleport) {
			loc.setY(loc.getY() + 1);
			teleport = checkForTeleportSpace(loc);
		}
		if(teleport) {
			player.setVelocity(new Vector());
			player.teleport(loc);
			SimpleAdminHacks.instance().log(Level.INFO, "Player '%s' %s: teleported to %s", player.getName(), reason, loc.toString());
			return true;
		}
		loc = getTeleportLocation(player, baseLoc.add(0, world.getMaxHeight() - baseLoc.getY(), 0));
		if(loc != null) {
			player.setVelocity(new Vector());
			loc.setX(Math.floor(loc.getX()) + 0.500000D);
	          loc.setY(loc.getY() + 1.02D);
	          loc.setZ(Math.floor(loc.getZ()) + 0.500000D);
			player.teleport(loc);
			SimpleAdminHacks.instance().log(Level.INFO, "Player '%s' %s: teleported to %s", player.getName(), reason, loc.toString());
			return true;
		}
		return false;
	}
}
