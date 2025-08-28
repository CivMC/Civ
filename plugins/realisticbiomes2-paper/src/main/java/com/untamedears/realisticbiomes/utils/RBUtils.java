package com.untamedears.realisticbiomes.utils;

import com.untamedears.realisticbiomes.growth.ColumnPlantGrower;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class RBUtils {

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

    public static int getVerticalSoilOffset(Material mat) {
        if (mat == Material.COCOA) {
            return -1;
        }
        return -2;
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

    public static boolean isFruit(Material mat) {
        return mat == Material.PUMPKIN || mat == Material.MELON;
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
