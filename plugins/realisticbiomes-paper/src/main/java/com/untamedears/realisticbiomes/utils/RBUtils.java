package com.untamedears.realisticbiomes.utils;

import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class RBUtils {

	public static final BlockFace[] cardinalSides = new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST,
			BlockFace.WEST };

	public static final BlockFace[] adjacentSides = new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST,
			BlockFace.WEST, BlockFace.DOWN, BlockFace.UP };

	public static boolean canGrowFruits(Material material) {
		return material == Material.MELON_STEM || material == Material.PUMPKIN_STEM;
	}

	public static Material getRemappedMaterial(Material mat) {
		switch (mat) {
		case CARROT:
		case CARROTS:
			return Material.CARROT;
		case POTATO:
		case POISONOUS_POTATO:
		case POTATOES:
			return Material.POTATO;
		case SUGAR_CANE:
			return Material.SUGAR_CANE;
		case MELON_SLICE:
		case MELON:
			return Material.MELON;
		case MELON_STEM:
		case ATTACHED_MELON_STEM:
		case MELON_SEEDS:
			return Material.MELON_SEEDS;
		case PUMPKIN:
			return Material.PUMPKIN;
		case PUMPKIN_SEEDS:
		case PUMPKIN_STEM:
		case ATTACHED_PUMPKIN_STEM:
			return Material.PUMPKIN_SEEDS;
		case WHEAT:
		case WHEAT_SEEDS:
			return Material.WHEAT;
		case CACTUS:
			return Material.CACTUS;
		case VINE:
			return Material.VINE;
		case OAK_LOG:
		case OAK_SAPLING:
		case OAK_LEAVES:
			return Material.OAK_SAPLING;
		case DARK_OAK_LOG:
		case DARK_OAK_SAPLING:
		case DARK_OAK_LEAVES:
			return Material.DARK_OAK_SAPLING;
		case BIRCH_LOG:
		case BIRCH_SAPLING:
		case BIRCH_LEAVES:
			return Material.BIRCH_SAPLING;
		case ACACIA_LOG:
		case ACACIA_SAPLING:
		case ACACIA_LEAVES:
			return Material.ACACIA_SAPLING;
		case JUNGLE_LOG:
		case JUNGLE_SAPLING:
		case JUNGLE_LEAVES:
			return Material.JUNGLE_SAPLING;
		case SPRUCE_LOG:
		case SPRUCE_SAPLING:
		case SPRUCE_LEAVES:
			return Material.SPRUCE_SAPLING;
		case NETHER_WART:
		case NETHER_WART_BLOCK:
			return Material.NETHER_WART;
		case BEETROOT:
		case BEETROOTS:
		case BEETROOT_SEEDS:
			return Material.BEETROOT;
		case COCOA:
		case COCOA_BEANS:
			return Material.COCOA;
		case BROWN_MUSHROOM:
			return Material.BROWN_MUSHROOM;
		case RED_MUSHROOM:
			return Material.RED_MUSHROOM;
			
		default:
			return null;
		}
	}

	public static int getVerticalSoilOffset(Material mat) {
		switch (mat) {
		case COCOA:
			return -1;
		default:
			return -2;
		}
	}

	/**
	 * Find an air block over dirt, grass or farmland adjacent to given block.
	 * 
	 * @param block Center to search from
	 * @return free block or null if none
	 */
	public static Block getViableFruitSpot(Block stemBlock) {
		for (BlockFace face : cardinalSides) {
			Block candidate = stemBlock.getRelative(face);
			if (candidate.getType() == Material.AIR) {
				Material soil = candidate.getRelative(BlockFace.DOWN).getType();
				if (soil == Material.DIRT || soil == Material.FARMLAND || soil == Material.GRASS) {
					return candidate;
				}
			}
		}
		return null;
	}

	public static boolean isBoneMealable(Material material) {
		return isCrop(material) || isSapling(material);
	}

	public static boolean isColumnPlant(Material material) {
		return material == Material.CACTUS || material == Material.SUGAR_CANE;
	}

	public static boolean isCrop(Material material) {
		return material == Material.BEETROOTS || material == Material.WHEAT || material == Material.POTATOES
				|| material == Material.CARROTS || material == Material.NETHER_WART_BLOCK;
	}

	public static boolean isSapling(Material material) {
		return material == Material.ACACIA_SAPLING || material == Material.BIRCH_SAPLING
				|| material == Material.DARK_OAK_SAPLING || material == Material.JUNGLE_SAPLING
				|| material == Material.OAK_SAPLING || material == Material.SPRUCE_SAPLING;
	}

	public static boolean isSoilBlock(Material material) {
		return material == Material.DIRT || material == Material.GRASS || material == Material.FARMLAND;
	}
	
	public static TreeType getTreeType(Block block) {
		switch(block.getType()) {
		case ACACIA_SAPLING:
			return TreeType.ACACIA;
		case BIRCH_SAPLING:
			return TreeType.BIRCH;
		case OAK_SAPLING:
			return TreeType.TREE;
		case JUNGLE_SAPLING:
			
		case DARK_OAK_SAPLING:
			return TreeType.DARK_OAK;
		case SPRUCE_SAPLING:
			
		}
	}
}
