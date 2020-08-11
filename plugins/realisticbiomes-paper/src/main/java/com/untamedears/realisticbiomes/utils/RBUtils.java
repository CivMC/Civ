package com.untamedears.realisticbiomes.utils;

import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.untamedears.realisticbiomes.growth.ColumnPlantGrower;

public class RBUtils {

	public static boolean canGrowFruits(Material material) {
		return material == Material.MELON_STEM || material == Material.PUMPKIN_STEM;
	}

	public static Material getFruit(Material mat) {
		switch (mat) {
		case MELON_SEEDS:
		case MELON_STEM:
			return Material.MELON;
		case PUMPKIN_SEEDS:
		case PUMPKIN_STEM:
			return Material.PUMPKIN;
		default:
			return null;
		}
	}

	public static Block getRealPlantBlock(Block block) {
		if (isColumnPlant(block.getType())) {
			return ColumnPlantGrower.getRelativeBlock(block, BlockFace.DOWN);
		}
		return block;
	}

	public static TreeType getTreeType(Block block) {
		switch (block.getType()) {
		case ACACIA_SAPLING:
			return TreeType.ACACIA;
		case BIRCH_SAPLING:
			return TreeType.BIRCH;
		case OAK_SAPLING:
			return TreeType.TREE;
		case JUNGLE_SAPLING:
			return TreeType.JUNGLE;
		case DARK_OAK_SAPLING:
			return TreeType.DARK_OAK;
		case SPRUCE_SAPLING:
			return TreeType.REDWOOD;
		default:
			throw new IllegalArgumentException();
		}
	}

	public static int getVerticalSoilOffset(Material mat) {
		if (mat == Material.COCOA) {
			return -1;
		}
		return -2;
	}

	public static boolean isBoneMealable(Material material) {
		return isCrop(material) || isSapling(material);
	}

	public static boolean isColumnPlant(Material mat) {
		return mat == Material.CACTUS || mat == Material.SUGAR_CANE  || mat == Material.BAMBOO;
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

	public static boolean isStem(Material mat) {
		return mat == Material.PUMPKIN_STEM || mat == Material.ATTACHED_PUMPKIN_STEM 
				|| mat == Material.MELON_STEM|| mat == Material.ATTACHED_MELON_STEM ; 
	}
	
	public static boolean isFruit(Material mat) {
		return mat == Material.PUMPKIN || mat == Material.MELON;
	}

	public static boolean resetProgressOnGrowth(Material mat) {
		return isColumnPlant(mat) || isStem(mat);
	}

	private RBUtils() {

	}
}
