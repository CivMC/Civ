package com.github.igotyou.FactoryMod.structures;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class BlockFurnaceStructure extends MultiBlockStructure {

	private Block center;
	private Block furnace;
	private boolean complete = false;

	public BlockFurnaceStructure(Block center) {
		if (center.getType() == Material.DROPPER) {
			this.center = center;
			for (Block b : searchForBlockOnSides(center, Material.FURNACE)) {
				furnace = b;
				complete = true;
				break;
			}
		}
	}
	
	public BlockFurnaceStructure(List <Block> blocks) {
		this.center = blocks.get(0);
		this.furnace = blocks.get(1);
	}

	public Location getCenter() {
		return center.getLocation();
	}
	
	public Block getFurnace() {
		return furnace;
	}

	public List<Block> getAllBlocks() {
		List<Block> blocks = new LinkedList<Block>();
		blocks.add(center);
		blocks.add(furnace);
		return blocks;
	}

	public boolean isComplete() {
		return complete;
	}

	public void recheckComplete() {
		complete = (center.getType() == Material.DROPPER && (furnace
				.getType() == Material.FURNACE || furnace.getType() == Material.BURNING_FURNACE));
	}

}
