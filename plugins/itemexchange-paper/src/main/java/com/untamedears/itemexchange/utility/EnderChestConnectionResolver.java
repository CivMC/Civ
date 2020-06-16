package com.untamedears.itemexchange.utility;

import com.untamedears.itemexchange.ItemExchangeConfig;
import com.untamedears.itemexchange.ItemExchangePlugin;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.PlayerInventory;
import vg.civcraft.mc.civmodcore.util.NullCoalescing;

import java.util.ArrayList;
import java.util.List;

public class EnderChestConnectionResolver {
	/**
	 * All six faces of a block.
	 */
	private static final BlockFace[] DIRECT_FACES = {
			BlockFace.UP,
			BlockFace.DOWN,
			BlockFace.NORTH,
			BlockFace.SOUTH,
			BlockFace.EAST,
			BlockFace.WEST
	};

	private static final ItemExchangePlugin PLUGIN = ItemExchangePlugin.getInstance();

	/**
	 * Finds connected inventories, using the following algorithm:
	 *
	 * For each direct face of the starting location, scan in a straight line up to the maximum blocks defined in the config.
	 *
	 * If a container sutible for a shop is found, add it to the list and stop the line.
	 *
	 * If a block as defined as a bounce block in the config is found, recurse using its location as the stopping location,
	 * adding each container found in the recursion and stopping the line.
	 *
	 * If another block is found that is not passable, stop the line.
	 *
	 * If the maximum scan distance has been reached, stop the line.
	 *
	 * After every side has been checked, return the list of inventories.
	 *
	 * @param startingLocation The location to scanning start from.
	 * @return All the inventories connected to the ender chest.
	 */
	public static List<Inventory> getConnectedInventories(Location startingLocation) {
		return getConnectedInventories(startingLocation,
				ItemExchangeConfig.getShopBounceMaxDistance(),
				ItemExchangeConfig.getShopBounceLimit(),
				ItemExchangeConfig.getShopBounceMaxContainers());
	}

	/**
	 * Execute the bouncing algorithm using the set options instead of the config.
	 * @param startingLocation The location to start scanning from.
	 * @param distanceLimit The maximum distance to scan out from one side of the startingLocation.
	 * @param recursionLimit The number of times to recurse.
	 * @param containerLimit The maximum number of inventories to find.
	 *                          If this number is exceeded, the algorithm will immediately stop.
	 * @return The inventories that have been found.
	 */
	public static List<Inventory> getConnectedInventories(Location startingLocation,
												   int distanceLimit,
												   int recursionLimit,
												   int containerLimit) {
		return getConnectedInventories(startingLocation,
				distanceLimit,
				recursionLimit,
				containerLimit,
				new ArrayList<>(),
				BlockFace.SELF);
	}

	/**
	 * Recursion target for the bounce algorithm.
	 * @param found The list of shop compatible containers that have been found so far.
	 * @param cameFrom The side of the block that the previous recursion found.
	 *                 This exists to prevent pseudo-infinite loops.
	 *                 SELF if this is the first recursion.
	 * @return `found`, with any newly found containers.
	 */
	private static List<Inventory> getConnectedInventories(Location enderChest,
													int distanceLimit,
													int recursionLimit,
													int containerLimit,
													List<Inventory> found,
													BlockFace cameFrom) {
		// Possibly there's a better way to limit containers than checking it 3 times?
		if (found.size() >= containerLimit) {
			PLUGIN.debug("Reached container limit A, stopping");
			return found;
		}

		if (recursionLimit <= 0) {
			PLUGIN.debug("Reached recursion limit A, stopping");
			return found;
		}

		for (BlockFace face : DIRECT_FACES) {
			if (face.equals(cameFrom)) {
				continue;
			}
			PLUGIN.debug("Checking in " + face);

			Location checkingLocation = enderChest.clone();
			for (int i = 0; i < distanceLimit; i++) {
				checkingLocation = checkingLocation.add(face.getModX(), face.getModY(), face.getModZ());
				Block block = checkingLocation.getBlock();

				if (ItemExchangeConfig.getShopBounceBlocks().contains(block.getType())) {
					// if is a bounce block
					PLUGIN.debug("Found bounce block");
					recursionLimit--;
					if (recursionLimit <= 0) {
						PLUGIN.debug("Reached recursion limit B, stopping");
						return found;
					}
					found = getConnectedInventories(checkingLocation,
							distanceLimit,
							recursionLimit,
							containerLimit,
							found,
							face.getOppositeFace());
					if (found.size() >= containerLimit) {
						PLUGIN.debug("Reached container limit B, stopping");
						return found;
					}
					break;
				} else if (ItemExchangeConfig.hasCompatibleShopBlock(block.getType())) {
					// if is a shop chest
					PLUGIN.debug("Found shop chest");
					found.add(NullCoalescing.chain(() -> ((InventoryHolder) block.getState()).getInventory()));
					if (found.size() >= containerLimit) {
						PLUGIN.debug("Reached container limit C, stopping");
						return found;
					}
					break;
				} else if (!block.isPassable()) {
					PLUGIN.debug("Found unpassable block, stopping");
					break;
				}
			}
		}

		PLUGIN.debug("Default return");
		return found;
	}
}
