package com.github.igotyou.FactoryMod.structures;

import java.util.LinkedList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Dispenser;
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
	private Material pipeMaterial;
	private boolean complete;

	@SuppressWarnings("deprecation")
	public PipeStructure(Block startBlock) {
		if (startBlock.getType() != Material.DISPENSER) {
			return;
		}
		this.start = startBlock.getLocation();
		for (Block b : MultiBlockStructure.searchForBlockOnAllSides(startBlock,
				Material.FURNACE)) {
			furnace = b.getLocation();
			break;
		}
		if (furnace == null) {
			return;
		}
		glassPipe = new LinkedList<>();
		Dispenser disp = (Dispenser) startBlock.getBlockData();
		Block currentBlock = startBlock.getRelative(disp.getFacing());
		pipeMaterial = currentBlock.getType();

		Block previousBlock = null;

		glassPipe.add(currentBlock.getLocation());
		int length = 1;
		while (length <= 512) {
			List<Block> blocks = MultiBlockStructure
					.getAdjacentBlocks(currentBlock);
			boolean foundEnd = false;
			boolean foundPipeBlock = false;
			for (Block b : blocks) {
				if (b.getState() instanceof InventoryHolder && !b.getLocation().equals(start)) {
					end = b.getLocation();
					this.length = length;
					complete = true;
					foundEnd = true;
					break;
				} else if (b.getType() == pipeMaterial
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
		List<Location> glass = new LinkedList<>();
		for (int i = 2; i < blocks.size()-1;i++) {
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
				&& furnace.getBlock().getType() != Material.FURNACE;
	}

	public List<Location> getAllBlocks() {
		List<Location> res = new LinkedList<>();
		res.add(start);
		res.add(furnace);
		res.addAll(glassPipe);
		res.add(end);
		return res;
	}

	public List<Block> getRelevantBlocks() {
		List<Block> res = new LinkedList<>();
		res.add(start.getBlock());
		res.add(furnace.getBlock());
		return res;
	}

	@SuppressWarnings("deprecation")
	public void recheckComplete() {
		if (start == null
				|| furnace == null
				|| end == null
				|| start.getBlock().getType() != Material.DISPENSER
				|| furnace.getBlock().getType() != Material.FURNACE
				|| !(end.getBlock().getState() instanceof InventoryHolder)) {
			complete = false;
			return;
		}
		for (Location loc : glassPipe) {
			Block b = loc.getBlock();
			if (b.getType() != pipeMaterial) {
				complete = false;
				return;
			}
		}
		complete = true;
	}

	public boolean isComplete() {
		return complete;
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

	public Material getPipeType() {
		return pipeMaterial;
	}

	public void setPipeType(Material pipeType) {
		pipeMaterial = pipeType;
	}
}
