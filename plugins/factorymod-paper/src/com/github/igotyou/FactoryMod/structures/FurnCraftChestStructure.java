package com.github.igotyou.FactoryMod.structures;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 * Physical representation of a factory consisting of a chest, a crafting table
 * and a furnace. The crafting table has to be inbetween the furnace and chest.
 * The chest may be a double chest, but the part of the double chest not
 * adjacent to the crafting table is ignored when doing any checks
 *
 */
public class FurnCraftChestStructure extends MultiBlockStructure {
	private Block CraftingTable;
	private Block Furnace;
	private Block Chest;

	public FurnCraftChestStructure(Block center) {
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
				case UP:
					if (center.getRelative(BlockFace.DOWN).getType() == Material.FURNACE) {
						Chest = b;
						Furnace = center.getRelative(BlockFace.DOWN);
					}
				case DOWN:
					if (center.getRelative(BlockFace.UP).getType() == Material.FURNACE) {
						Chest = b;
						Furnace = center.getRelative(BlockFace.UP);
					}
				}

			}
		}
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
