package com.untamedears.realisticbiomes.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.TreeType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import com.untamedears.realisticbiomes.GrowthMap;
import com.untamedears.realisticbiomes.RealisticBiomes;

public class Trees {
	
	// positions of a 2x2 array, the center is the sapling northwest
	public static List<Vector> largeTreeBlocks = new ArrayList<Vector>();
	static {
		largeTreeBlocks.add(new Vector(1,0,0));	// east
		largeTreeBlocks.add(new Vector(0,0,1));	// south
		largeTreeBlocks.add(new Vector(1,0,1));	// southeast
	}
	
	// possible positions of 2x2 centers for a block
	static List<Vector> largeTreeOriginBlocks = new ArrayList<Vector>();
	static {
		largeTreeOriginBlocks.add(new Vector(-1,0,0));	// west
		largeTreeOriginBlocks.add(new Vector(0,0,-1));	// north
		largeTreeOriginBlocks.add(new Vector(-1,0,-1));	// northwest
	}

	public static TreeType getTreeType(Block block, GrowthMap growthMap) {
		TreeType type = MaterialAliases.getTreeType(block);
		
		if (type == TreeType.JUNGLE) {
			if (!canGrowLArge(block, type)) {
				// if a tree can't actually be a (large) JUNGLE tree, change to SMALL_JUNGLE unless part of a 2x2 array
				if (isPartOfLargeTree(block, type)) {
					// part of 2x2 array, abort and don't mess it up
					return null;
				} else {
					// was not part of any 2x2 array, and itself is not the northwest (center) of a 2x2, switch to SMALL_JUNGLE
					type = TreeType.SMALL_JUNGLE;
				}

			} else {			
				RealisticBiomes.doLog(Level.FINER, "getTreeType jungle tall");
			}

		} else if (type == TreeType.REDWOOD) {
			if (canGrowLArge(block, type)) {
				RealisticBiomes.doLog(Level.FINER, "getTreeType mega redwood");
				type = TreeType.MEGA_REDWOOD;
				
			} else if (isPartOfLargeTree(block, type)) {
				// part of 2x2 array, abort and don't mess it up
				return null;
			}

		} else if (type == TreeType.DARK_OAK) {
			if (!canGrowLArge(block, type)) {
				RealisticBiomes.doLog(Level.FINER, "getTreeType darkoak not 2x2");
				return null;
			}
			
		} else if (type == TreeType.TREE) {
			if (block.getBiome() == Biome.SWAMPLAND || block.getBiome() == Biome.MUTATED_SWAMPLAND) {
				// swamptree, only spawns naturally at worldgen
				type = TreeType.SWAMP;
				RealisticBiomes.doLog(Level.FINER, "getTreeType swamp");
			}

		}
		
		RealisticBiomes.doLog(Level.FINER, "Trees.getTreeType(): " + type);
		
		return type;
	}

	public static TreeType getAlternativeTree(TreeType treeType, Block block, GrowthMap growthMap) {
		TreeType altType;
		if (treeType == TreeType.BIRCH) {
			altType = TreeType.TALL_BIRCH;
		} else if (treeType == TreeType.TREE) {
			altType = TreeType.BIG_TREE;
		} else if (treeType == TreeType.SMALL_JUNGLE) {
			altType = TreeType.JUNGLE_BUSH;
		} else if (treeType == TreeType.REDWOOD) {
			altType = TreeType.TALL_REDWOOD;
		} else {
			return treeType;
		}
		
		double rate = growthMap.get(altType).getRate(block);
		RealisticBiomes.doLog(Level.FINER, "Trees.getChance() is " + rate + " for " + altType + " at " + block.getLocation());
		
		if (Math.random() < rate) {
			return altType;
		} else {
			return treeType;
		}
	}

	public static Block getLargeTreeOrigin(Block block, TreeType type) {
		type = getSaplingType(type);
		for (Vector vec: largeTreeOriginBlocks) {
			Block candidate = block.getLocation().add(vec).getBlock();
			if (MaterialAliases.getTreeType(candidate) == type && canGrowLArge(candidate, type)) {
				return candidate;
			}
		}
		return null;
	}

	private static boolean isPartOfLargeTree(Block block, TreeType type) {
		return getLargeTreeOrigin(block, type) != null;
	}

	public static boolean canGrowLArge(Block block, TreeType type) {
		type = getSaplingType(type);
		for (Vector vec: largeTreeBlocks) {
			Block candidate = block.getLocation().add(vec).getBlock();
			if (MaterialAliases.getTreeType(candidate) != type) {
				return false;
			}
		}
		return true;
	}

	private static TreeType getSaplingType(TreeType type) {
		if (type == TreeType.MEGA_REDWOOD) {
			return TreeType.REDWOOD;
		} else {
			return type;
		}
	}
}
