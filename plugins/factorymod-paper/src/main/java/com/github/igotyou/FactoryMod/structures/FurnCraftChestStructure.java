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
	private Location craftingTable;
	private Location furnace;
	private Location chest;
	private boolean complete;

	public FurnCraftChestStructure(Block center) {
		if (center.getType() == Material.WORKBENCH) {
			craftingTable = center.getLocation();
			for (Block b : searchForBlockOnAllSides(center, Material.CHEST)) {
				switch (center.getFace(b)) {
				case SOUTH:
					if (center.getRelative(BlockFace.NORTH).getType() == Material.FURNACE) {
						chest = b.getLocation();
						furnace = center.getRelative(BlockFace.NORTH)
								.getLocation();
					}
					break;
				case NORTH:
					if (center.getRelative(BlockFace.SOUTH).getType() == Material.FURNACE) {
						chest = b.getLocation();
						furnace = center.getRelative(BlockFace.SOUTH)
								.getLocation();
					}
					break;
				case WEST:
					if (center.getRelative(BlockFace.EAST).getType() == Material.FURNACE) {
						chest = b.getLocation();
						furnace = center.getRelative(BlockFace.EAST)
								.getLocation();
					}
					break;
				case EAST:
					if (center.getRelative(BlockFace.WEST).getType() == Material.FURNACE) {
						chest = b.getLocation();
						furnace = center.getRelative(BlockFace.WEST)
								.getLocation();
					}
					break;
				case UP:
					if (center.getRelative(BlockFace.DOWN).getType() == Material.FURNACE) {
						chest = b.getLocation();
						furnace = center.getRelative(BlockFace.DOWN)
								.getLocation();
					}
					break;
				case DOWN:
					if (center.getRelative(BlockFace.UP).getType() == Material.FURNACE) {
						chest = b.getLocation();
						furnace = center.getRelative(BlockFace.UP)
								.getLocation();
					}
					break;
				}

			}
		}
		if (chest != null && furnace != null) {
			complete = true;
		} else {
			complete = false;
		}
	}

	public void recheckComplete() {
		complete = craftingTable != null
				&& craftingTable.getBlock().getType() == Material.WORKBENCH
				&& furnace != null
				&& (furnace.getBlock().getType() == Material.FURNACE || furnace
						.getBlock().getType() == Material.BURNING_FURNACE)
				&& chest != null
				&& chest.getBlock().getType() == Material.CHEST;
	}

	public FurnCraftChestStructure(List<Location> blocks) {
		craftingTable = blocks.get(0);
		furnace = blocks.get(1);
		chest = blocks.get(2);
	}

	public boolean isComplete() {
		return complete;
	}

	public Block getCraftingTable() {
		return craftingTable.getBlock();
	}

	public Block getFurnace() {
		return furnace.getBlock();
	}

	public Block getChest() {
		// sometimes a double chest will go across chunk borders and the other
		// half of the chest might be unloaded. To load the other half and the
		// full inventory this is needed to load the chunk
		MultiBlockStructure.searchForBlockOnAllSides(chest.getBlock(),
				Material.CHEST);
		return chest.getBlock();
	}

	public boolean relevantBlocksDestroyed() {
		return craftingTable.getBlock().getType() != Material.WORKBENCH
				&& furnace.getBlock().getType() != Material.FURNACE
				&& furnace.getBlock().getType() != Material.BURNING_FURNACE
				&& chest.getBlock().getType() != Material.CHEST;
	}

	public List<Block> getRelevantBlocks() {
		LinkedList<Block> result = new LinkedList<Block>();
		result.add(getCraftingTable());
		result.add(getFurnace());
		result.add(getChest());
		return result;
	}

	public List<Location> getAllBlocks() {
		LinkedList<Location> result = new LinkedList<Location>();
		result.add(craftingTable);
		result.add(furnace);
		result.add(chest);
		return result;
	}

	public Location getCenter() {
		return chest;
	}

}
