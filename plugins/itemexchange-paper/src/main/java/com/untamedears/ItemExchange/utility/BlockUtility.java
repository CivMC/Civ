package com.untamedears.ItemExchange.utility;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.inventory.*;

import com.untamedears.ItemExchange.DeprecatedMethods;
import com.untamedears.ItemExchange.ItemExchangePlugin;

public class BlockUtility {

	public static final BlockFace[] CardinalFaces = new BlockFace[] {
			BlockFace.NORTH,
			BlockFace.EAST,
			BlockFace.SOUTH,
			BlockFace.WEST,
			BlockFace.DOWN,
			BlockFace.UP
	};

	public static BlockFace GetFacingDirection(Block block) {
		byte data = DeprecatedMethods.getBlockMeta(block);
		if ((data & 0x5) == 5)
			return BlockFace.EAST;
		else if((data & 0x4) == 4)
			return BlockFace.WEST;
		else if((data & 0x3) == 3)
			return BlockFace.SOUTH;
		else if((data & 0x2) == 2)
			return BlockFace.NORTH;
		else if((data & 0x1) == 1)
			return BlockFace.UP;
		else if((data & 0x0) == 0)
			return BlockFace.DOWN;
		else
			return BlockFace.SELF;
	}

	public static BlockFace GetAttachedDirection(Block block) {
		byte data = DeprecatedMethods.getBlockMeta(block);
		if ((data & 0x5) == 5)
			return BlockFace.UP;
		else if((data & 0x4) == 4)
			return BlockFace.NORTH;
		else if((data & 0x3) == 3)
			return BlockFace.SOUTH;
		else if((data & 0x2) == 2)
			return BlockFace.WEST;
		else if((data & 0x1) == 1)
			return BlockFace.EAST;
		else if((data & 0x0) == 0)
			return BlockFace.DOWN;
		else
			return BlockFace.SELF;
	}

	public static void PowerBlock(Block block, long time) {
		// Ensure that timedelay exists
		if (time < 0) time = 0;
		// Set the block to be powered
		BlockState B_OnState = block.getState();
		byte B_OnMeta = DeprecatedMethods.getBlockMeta(block);
		DeprecatedMethods.setBlockMeta(B_OnState, (byte)(B_OnMeta | 0x8));
		B_OnState.update();
		// And set the block to unpower in due time
		Bukkit.getScheduler().scheduleSyncDelayedTask(ItemExchangePlugin.instance, new Runnable() {
			public void run() {
				BlockState B_OffState = block.getState();
				byte B_OffMeta = DeprecatedMethods.getBlockMeta(block);
				DeprecatedMethods.setBlockMeta(B_OffState, (byte)(B_OffMeta & ~0x8));
				B_OffState.update();
			}
		}, time);
	}

	public static boolean IsShopCompatible(Material mat) {
		for (Material item : ItemExchangePlugin.ACCEPTABLE_BLOCKS) {
			if (mat == item)
				return true;
		}
		return false;
	}

	public static BlockFace[] GetPerpendicularFaces(final BlockFace reference) {
		if (reference == BlockFace.NORTH || reference == BlockFace.SOUTH)
			return new BlockFace[] { BlockFace.EAST, BlockFace.WEST };
		else if (reference == BlockFace.EAST || reference == BlockFace.WEST)
			return new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH };
		else
			return null;
	}

	public static Block CheckIfDoubleChest(final Block chest) {
		BlockState C_State = chest.getState();
		// If block is not a double chest, then do nothing
		if (!(C_State instanceof Chest)) return null;
		// Otherwise get the locations of both sides
		Inventory C_Invetory = ((Chest)C_State).getInventory();
		if (!(C_Invetory instanceof DoubleChestInventory)) return null;
		DoubleChestInventory DC_Inventory = (DoubleChestInventory)C_Invetory;
		Location DC_LSLocation = DC_Inventory.getLeftSide().getLocation();
		Location DC_RSLocation = DC_Inventory.getRightSide().getLocation();
		// If LeftSide has the same location as the original chest, use RightSize
		if (chest.getLocation().equals(DC_LSLocation))
			return DC_RSLocation.getBlock();
		else
			return DC_LSLocation.getBlock();
	}
}
