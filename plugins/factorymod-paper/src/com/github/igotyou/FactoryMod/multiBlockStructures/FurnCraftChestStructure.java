package com.github.igotyou.FactoryMod.multiBlockStructures;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class FurnCraftChestStructure extends MultiBlockStructure {
	public boolean isComplete(Block center) {
		if (center.getType() == Material.WORKBENCH) {
			for (Block b : searchForBlockOnSides(center, Material.CHEST)) {
				switch (center.getFace(b)) {
				case SOUTH:
					if (center.getRelative(BlockFace.NORTH).getType() == Material.FURNACE) {
						return true;
					}
				case NORTH:
					if (center.getRelative(BlockFace.SOUTH).getType() == Material.FURNACE) {
						return true;
					}
				case WEST:
					if (center.getRelative(BlockFace.EAST).getType() == Material.FURNACE) {
						return true;
					}
				case EAST:
					if (center.getRelative(BlockFace.WEST).getType() == Material.FURNACE) {
						return true;
					}
				}

			}
		}

		return false;
	}
	
	public Block getInventoryBlock(Block center) {
		if (center.getType() == Material.WORKBENCH) {
			for (Block b : searchForBlockOnSides(center, Material.CHEST)) {
				switch (center.getFace(b)) {
				case SOUTH:
					if (center.getRelative(BlockFace.NORTH).getType() == Material.FURNACE) {
						return b;
					}
				case NORTH:
					if (center.getRelative(BlockFace.SOUTH).getType() == Material.FURNACE) {
						return b;
					}
				case WEST:
					if (center.getRelative(BlockFace.EAST).getType() == Material.FURNACE) {
						return b;
					}
				case EAST:
					if (center.getRelative(BlockFace.WEST).getType() == Material.FURNACE) {
						return b;
					}
				}

			}
		}

		return null;
	}
	
	
	public Block getFurnace(Block center) {
		if (center.getType() == Material.WORKBENCH) {
			for (Block b : searchForBlockOnSides(center, Material.FURNACE)) {
				switch (center.getFace(b)) {
				case SOUTH:
					if (center.getRelative(BlockFace.NORTH).getType() == Material.CHEST) {
						return b;
					}
				case NORTH:
					if (center.getRelative(BlockFace.SOUTH).getType() == Material.CHEST) {
						return b;
					}
				case WEST:
					if (center.getRelative(BlockFace.EAST).getType() == Material.CHEST) {
						return b;
					}
				case EAST:
					if (center.getRelative(BlockFace.WEST).getType() == Material.CHEST) {
						return b;
					}
				}

			}
		}

		return null;
	}

}
