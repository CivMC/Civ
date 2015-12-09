package com.github.igotyou.FactoryMod.multiBlockStructures;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class FurnCraftChestStructure extends MultiBlockStructure {
	private Block CraftingTable;
	private Block Furnace;
	private Block Chest;

	public FurnCraftChestStructure(Block center) {
		initializeBlocks(center);
	}

	public boolean isComplete() {
		return CraftingTable.getType() == Material.WORKBENCH
				&& (Furnace.getType() == Material.FURNACE || Furnace.getType() == Material.BURNING_FURNACE)
				&& Chest.getType() == Material.CHEST;
	}

	public void initializeBlocks(Block center) {
		if (center.getType() == Material.WORKBENCH) {
			CraftingTable = center;
			for (Block b : searchForBlockOnSides(center, Material.CHEST)) {
				switch (center.getFace(b)) {
				case SOUTH:
					if (center.getRelative(BlockFace.NORTH).getType() == Material.FURNACE) {
						Chest = b;
						Furnace = center.getRelative(BlockFace.NORTH);
					}
				case NORTH:
					if (center.getRelative(BlockFace.SOUTH).getType() == Material.FURNACE) {
						Chest = b;
						Furnace = center.getRelative(BlockFace.SOUTH);
					}
				case WEST:
					if (center.getRelative(BlockFace.EAST).getType() == Material.FURNACE) {
						Chest = b;
						Furnace = center.getRelative(BlockFace.EAST);
					}
				case EAST:
					if (center.getRelative(BlockFace.WEST).getType() == Material.FURNACE) {
						Chest = b;
						Furnace = center.getRelative(BlockFace.WEST);
					}
				}

			}
		}
	}

	public Block getCraftingTable() {
		return CraftingTable;
	}

	public Block getFurnace() {
		return Furnace;
	}

	public Block getChest() {
		return Chest;
	}

	public List<Block> getAllBlocks() {
		LinkedList<Block> result = new LinkedList<Block>();
		result.add(Furnace);
		result.add(Chest);
		result.add(CraftingTable);
		return result;
	}

}
