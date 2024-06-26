package com.untamedears.realisticbiomes.utils;

import com.untamedears.realisticbiomes.growth.ColumnPlantGrower;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

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
            return ColumnPlantGrower.getRelativeBlock(block, RBUtils.getGrowthDirection(block.getType()).getOppositeFace());
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
            case CRIMSON_FUNGUS:
                return TreeType.CRIMSON_FUNGUS;
            case WARPED_FUNGUS:
                return TreeType.WARPED_FUNGUS;
            case FLOWERING_AZALEA:
                return TreeType.AZALEA;
            case MANGROVE_PROPAGULE:
                return TreeType.MANGROVE;
            case CHERRY_SAPLING:
                return TreeType.CHERRY;
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
        return mat == Material.CACTUS || mat == Material.SUGAR_CANE || mat == Material.BAMBOO
            || mat == Material.TWISTING_VINES || mat == Material.WEEPING_VINES
            || mat == Material.TWISTING_VINES_PLANT || mat == Material.WEEPING_VINES_PLANT
            || mat == Material.KELP || mat == Material.KELP_PLANT
            || mat == Material.CAVE_VINES || mat == Material.CAVE_VINES_PLANT
            || mat == Material.BIG_DRIPLEAF || mat == Material.BIG_DRIPLEAF_STEM;
    }

    public static Material getStemMaterial(Material material) {
        switch (material) {
            case KELP:
                return Material.KELP_PLANT;
            case TWISTING_VINES:
                return Material.TWISTING_VINES_PLANT;
            case WEEPING_VINES:
                return Material.WEEPING_VINES_PLANT;
            case BAMBOO_SAPLING:
                return Material.BAMBOO;
            case CAVE_VINES:
                return Material.CAVE_VINES_PLANT;
            case BIG_DRIPLEAF:
                return Material.BIG_DRIPLEAF_STEM;
            default:
                return material;
        }
    }

    public static Material getTipMaterial(Material material) {
        switch (material) {
            case KELP_PLANT:
                return Material.KELP;
            case TWISTING_VINES_PLANT:
                return Material.TWISTING_VINES;
            case WEEPING_VINES_PLANT:
                return Material.WEEPING_VINES;
            case CAVE_VINES_PLANT:
                return Material.CAVE_VINES;
            case BIG_DRIPLEAF_STEM:
                return Material.BIG_DRIPLEAF;
            default:
                return material;
        }
    }

    public static boolean isCrop(Material material) {
        return material == Material.BEETROOTS || material == Material.WHEAT || material == Material.POTATOES
            || material == Material.CARROTS || material == Material.NETHER_WART_BLOCK;
    }

    public static boolean isSapling(Material material) {
        return material == Material.ACACIA_SAPLING || material == Material.BIRCH_SAPLING
            || material == Material.DARK_OAK_SAPLING || material == Material.JUNGLE_SAPLING
            || material == Material.OAK_SAPLING || material == Material.SPRUCE_SAPLING
            || material == Material.CRIMSON_FUNGUS || material == Material.WARPED_FUNGUS
            || material == Material.FLOWERING_AZALEA || material == Material.MANGROVE_PROPAGULE
            || material == Material.CHERRY_SAPLING;
    }

    public static boolean isStem(Material mat) {
        return mat == Material.PUMPKIN_STEM || mat == Material.ATTACHED_PUMPKIN_STEM
            || mat == Material.MELON_STEM || mat == Material.ATTACHED_MELON_STEM;
    }

    public static boolean isFruit(Material mat) {
        return mat == Material.PUMPKIN || mat == Material.MELON;
    }

    public static boolean resetProgressOnGrowth(Material mat) {
        return isColumnPlant(mat) || isStem(mat);
    }

    public static BlockFace getGrowthDirection(Material material) {
        switch (material) {
            case TWISTING_VINES:
            case TWISTING_VINES_PLANT:
            case KELP:
            case KELP_PLANT:
            case BAMBOO_SAPLING:
            case BAMBOO:
            case SUGAR_CANE:
            case CACTUS:
            case BIG_DRIPLEAF:
            case BIG_DRIPLEAF_STEM:
                return BlockFace.UP;
            case WEEPING_VINES:
            case WEEPING_VINES_PLANT:
            case CAVE_VINES:
            case CAVE_VINES_PLANT:
                return BlockFace.DOWN;
            default:
                return BlockFace.SELF;
        }
    }

    private RBUtils() {

    }
}
