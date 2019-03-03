package com.programmerdan.minecraft.simpleadminhacks.util;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;

public class TeleportUtil {
	private static boolean checkForTeleportSpace(Location loc) {
		final Block block = loc.getBlock();
		final Material mat = block.getType();
		if (mat.isSolid()) {
			return false;
		}
		final Block above = block.getRelative(BlockFace.UP);
		if (above.getType().isSolid()) {
			return false;
		}
		return true;
	}

	public static boolean tryToTeleportVertically(Player player, Location location, String reason) {
		Location loc = location.clone();
		loc.setX(Math.floor(loc.getX()) + 0.500000D);
		loc.setY(Math.floor(loc.getY()) + 0.02D);
		loc.setZ(Math.floor(loc.getZ()) + 0.500000D);
		final Location baseLoc = loc.clone();
		final World world = baseLoc.getWorld();
		// Check if teleportation here is viable
		boolean performTeleport = checkForTeleportSpace(loc);
		if (!performTeleport) {
			loc.setY(loc.getY() + 1.000000D);
			performTeleport = checkForTeleportSpace(loc);
		}
		if (performTeleport) {
			player.setVelocity(new Vector());
			player.teleport(loc);
			SimpleAdminHacks.instance().log(String.format(
				"Player '%s' %s: Teleported to %s",
				player.getName(), reason, loc.toString()));
			return true;
		}
		loc = baseLoc.clone();
		// Create a sliding window of block types and track how many of those
		//  are solid. Keep fetching the block below the current block to move down.
		int air_count = 0;
		LinkedList<Material> air_window = new LinkedList<Material>();
		loc.setY((float)world.getMaxHeight() - 2);
		Block block = world.getBlockAt(loc);
		for (int i = 0; i < 4; ++i) {
			Material block_mat = block.getType();
			if (!block_mat.isSolid()) {
				++air_count;
			}
			air_window.addLast(block_mat);
			block = block.getRelative(BlockFace.DOWN);
		}
		// Now that the window is prepared, scan down the Y-axis.
		while (block.getY() >= 1) {
			Material block_mat = block.getType();
			if (block_mat.isSolid()) {
				if (air_count == 4) {
					player.setVelocity(new Vector());
					loc = block.getLocation();
					loc.setX(Math.floor(loc.getX()) + 0.500000D);
					loc.setY(loc.getY() + 1.02D);
					loc.setZ(Math.floor(loc.getZ()) + 0.500000D);
					player.teleport(loc);
					SimpleAdminHacks.instance().log(String.format(
						"Player '%s' %s: Teleported to %s",
						player.getName(), reason, loc.toString()));
					return true;
				}
			} else {
				++air_count;
			}
			air_window.addLast(block_mat);
			if (!air_window.removeFirst().isSolid()) {
				--air_count;
			}
			block = block.getRelative(BlockFace.DOWN);
		}
		return false;
	}
}
