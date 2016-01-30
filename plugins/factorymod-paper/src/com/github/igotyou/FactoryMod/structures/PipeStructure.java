package com.github.igotyou.FactoryMod.structures;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;

/**
 * Represents a pipe with a dispenser at each end, which are directly connected
 * through blocks of one specific type which make up the actual pipe
 *
 */
public class PipeStructure extends MultiBlockStructure {
	private Location start;
	private Location furnace;
	private Location end;
	private int length;
	private List<Location> glassPipe;
	private byte glassColor;
	private static Material pipeMaterial = Material.STAINED_GLASS;
	private static int maximumLength = 128;
	private boolean complete;

	public PipeStructure(Block startBlock) {
		if (startBlock.getType() != Material.DISPENSER) {
			return;
		}
		this.start = startBlock.getLocation();
		for (Block b : MultiBlockStructure.searchForBlockOnSides(startBlock,
				Material.FURNACE)) {
			furnace = b.getLocation();
			break;
		}
		if (furnace == null) {
			return;
		}
		glassPipe = new LinkedList<Location>();
		Block currentBlock = startBlock.getRelative(dataBlockFaceConversion
				.get((int) (startBlock.getState().getRawData())));
		Block previousBlock = null;
		if (currentBlock.getType() != pipeMaterial) {
			return;
		}
		glassColor = currentBlock.getData();
		glassPipe.add(currentBlock.getLocation());
		int length = 1;
		while (length <= maximumLength) {
			List<Block> blocks = MultiBlockStructure
					.getAdjacentBlocks(currentBlock);
			boolean foundEnd = false;
			boolean foundPipeBlock = false;
			for (Block b : blocks) {
				if (b.getState() instanceof InventoryHolder) {
					end = b.getLocation();
					this.length = length;
					complete = true;
					foundEnd = true;
					break;
				} else if (b.getType() == pipeMaterial
						&& b.getData() == glassColor
						&& !b.equals(previousBlock)) {
					glassPipe.add(b.getLocation());
					previousBlock = currentBlock;
					currentBlock = b;
					length++;
					foundPipeBlock = true;
					break;
				}
			}
			if (foundEnd || !foundPipeBlock) {
				break;
			}
		}
	}

	public PipeStructure(List<Location> blocks) {
		this.start = blocks.get(0);
		this.furnace = blocks.get(1);
		this.end = blocks.get(blocks.size() - 1);
		List<Location> glass = new LinkedList<Location>();
		for (int i = 3; i< blocks.size()-1;i++) {
			glass.add(blocks.get(i));
		}
		this.glassPipe = glass;
		length = glassPipe.size();
		recheckComplete();
	}

	public Location getCenter() {
		return start;
	}

	public boolean relevantBlocksDestroyed() {
		return start.getBlock().getType() != Material.DISPENSER
				&& furnace.getBlock().getType() != Material.FURNACE
				&& furnace.getBlock().getType() != Material.BURNING_FURNACE;
	}

	public List<Location> getAllBlocks() {
		List<Location> res = new LinkedList<Location>();
		res.add(start);
		res.add(furnace);
		res.addAll(glassPipe);
		res.add(end);
		return res;
	}

	public List<Block> getRelevantBlocks() {
		List<Block> res = new LinkedList<Block>();
		res.add(start.getBlock());
		res.add(furnace.getBlock());
		return res;
	}

	public void recheckComplete() {
		if (start == null
				|| furnace == null
				|| end == null
				|| start.getBlock().getType() != Material.DISPENSER
				|| (furnace.getBlock().getType() != Material.FURNACE && furnace.getBlock().getType() != Material.BURNING_FURNACE)
				|| !(end.getBlock().getState() instanceof InventoryHolder)) {
			complete = false;
			return;
		}
		for (Location loc : glassPipe) {
			Block b = loc.getBlock();
			if (b.getType() != pipeMaterial || b.getData() != glassColor) {
				complete = false;
				return;
			}
		}
		complete = true;
	}

	public boolean isComplete() {
		return complete;
	}

	public byte getGlassColor() {
		return glassColor;
	}

	public int getLength() {
		return length;
	}

	public Block getStart() {
		return start.getBlock();
	}

	public Block getEnd() {
		return end.getBlock();
	}

	public Block getFurnace() {
		return furnace.getBlock();
	}
}
