package com.github.igotyou.FactoryMod.structures;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class BlockFurnaceStructure extends MultiBlockStructure {

	private Location center;
	private Location furnace;
	private boolean complete = false;

	public BlockFurnaceStructure(Block center) {
		if (center.getType() == Material.DROPPER) {
			this.center = center.getLocation();
			for (Block b : searchForBlockOnSides(center, Material.FURNACE)) {
				furnace = b.getLocation();
				complete = true;
				break;
			}
		}
	}

	public BlockFurnaceStructure(List<Location> blocks) {
		this.center = blocks.get(0);
		this.furnace = blocks.get(1);
	}

	public boolean relevantBlocksDestroyed() {
		return center.getBlock().getType() != Material.DROPPER
				&& furnace.getBlock().getType() != Material.FURNACE
				&& furnace.getBlock().getType() != Material.BURNING_FURNACE;
	}

	public Location getCenter() {
		return center;
	}

	public Block getFurnace() {
		return furnace.getBlock();
	}

	public List<Location> getAllBlocks() {
		List<Location> blocks = new LinkedList<Location>();
		blocks.add(center);
		blocks.add(furnace);
		return blocks;
	}

	public boolean isComplete() {
		return complete;
	}

	public void recheckComplete() {
		complete = (center.getBlock().getType() == Material.DROPPER && (furnace
				.getBlock().getType() == Material.FURNACE || furnace.getBlock()
				.getType() == Material.BURNING_FURNACE));
	}

	public List<Block> getRelevantBlocks() {
		List<Block> blocks = new LinkedList<Block>();
		blocks.add(center.getBlock());
		blocks.add(furnace.getBlock());
		return blocks;
	}

}
