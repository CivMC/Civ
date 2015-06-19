package com.untamedears.realisticbiomes.persist;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

/**
 * Fruit related helper functions. This could be optimized: convert this
 * into non-static, and create and instance per check that caches surrounding
 * block materials etc.
 */
public class Fruits {

	static List<Vector> surroundingBlocks = new ArrayList<Vector>();
	static {
		surroundingBlocks.add(new Vector(-1,0,0));	// west
		surroundingBlocks.add(new Vector(1,0,0));	// east
		surroundingBlocks.add(new Vector(0,0,-1));	// north
		surroundingBlocks.add(new Vector(0,0,1));	// south
	}
	
	public static boolean isFruitFul(Material material) {
		return material == Material.MELON_STEM || material == Material.PUMPKIN_STEM;
	}
	
	public static boolean isFruit(Material material) {
		return material == Material.MELON_BLOCK || material == Material.PUMPKIN;
	}

	public static boolean hasFruit(Block block) {
		return hasFruit(block, null);
	}

	/**
	 * Check if stem at block has ANY fruit next to it.
	 * @param block
	 * @param blockToIgnore Ignore this block for this check
	 * @return true if the stem has a fruit
	 */
	public static boolean hasFruit(Block block, Block blockToIgnore) {
		Material fruit = getFruit(block.getType());
		for(Vector vec : surroundingBlocks) {
			Block candidate = block.getLocation().add(vec).getBlock();
			if (blockToIgnore != null && candidate.getLocation().equals(blockToIgnore.getLocation())) {
				continue;
			}
			if (candidate.getType() == fruit) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Find an air block over dirt, grass or farmland adjacent to given block.
	 * @param block Center to search from
	 * @param blockToIgnore Ignore these possibly adjacent blocks
	 * @return free block or null if none
	 */
	public static Block getFreeBlock(Block block, Block blockToIgnore) {
		Block air = null;
		for( Vector vec : surroundingBlocks ) {
			Block candidate = block.getLocation().add(vec).getBlock();
			if (candidate.getType() == Material.AIR
					|| (blockToIgnore != null && candidate.getLocation().equals(blockToIgnore.getLocation()))) {
				Material soil = candidate.getRelative(BlockFace.DOWN).getType();
				if (soil == Material.DIRT || soil == Material.SOIL || soil == Material.GRASS) {
					air = candidate;
				}
			}
		}
		return air;
	}

	public static Material getFruit(Material material) {
		if (material == Material.PUMPKIN_STEM) {
			return Material.PUMPKIN;
		} else if (material == Material.MELON_STEM) {
			return Material.MELON_BLOCK;
		} else {
			return null;
		}
	}
	
	public static Material getStem(Material material) {
		if (material == Material.PUMPKIN) {
			return Material.PUMPKIN_STEM;
		} else if (material == Material.MELON_BLOCK) {
			return Material.MELON_STEM;
		} else {
			return null;
		}
	}

	/**
	 * Get a list of stems adjacent to given block
	 * @param block
	 * @return List of stems, can have from zero to four entries
	 */
	public static List<Block> getStems(Block block) {
		Material stem = getStem(block.getType());
		ArrayList<Block> candidates = new ArrayList<Block>();
		for( Vector vec : surroundingBlocks ) {
			Block candidate = block.getLocation().add(vec).getBlock();
			if (candidate.getType() == stem && BlockGrower.getGrowthFraction(candidate) >= 1.0) {
				if (!Fruits.hasFruit(candidate, block)) {
					candidates.add(candidate);
				}
			}
		}
		return candidates;
	}

}
