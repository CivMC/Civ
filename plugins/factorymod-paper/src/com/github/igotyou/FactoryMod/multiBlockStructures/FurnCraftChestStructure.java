package com.github.igotyou.FactoryMod.multiBlockStructures;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
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

	public FurnCraftChestStructure(List<Block> blocks) {
		CraftingTable = blocks.get(0);
		Furnace = blocks.get(1);
		Chest = blocks.get(2);
	}

	public boolean isComplete() {
		return CraftingTable != null
				&& CraftingTable.getType() == Material.WORKBENCH
				&& Furnace != null
				&& (Furnace.getType() == Material.FURNACE || Furnace.getType() == Material.BURNING_FURNACE)
				&& Chest != null && Chest.getType() == Material.CHEST;
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
		result.add(CraftingTable);
		result.add(Furnace);
		result.add(Chest);
		return result;
	}

	public Location getCenter() {
		return Chest.getLocation();
	}

}
