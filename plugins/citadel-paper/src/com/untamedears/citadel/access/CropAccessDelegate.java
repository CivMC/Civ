package com.untamedears.citadel.access;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.MaterialData;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.Utility;

public class CropAccessDelegate extends AccessDelegate<MaterialData> {

    private Block soil;

    public CropAccessDelegate(Block block, MaterialData data) {
        super(block, data);
    }

    public static Block findPlantSoil(Block plant) {
        Material mat = plant.getType();
        if (isSoilPlant(mat)) {
            return findSoilBelow(plant, Material.SOIL);
        }
        if (isDirtPlant(mat)) {
            return findSoilBelow(plant, Material.DIRT);
        }
        if (isSandPlant(mat)) {
            return findSoilBelow(plant, Material.SAND);
        }
        if (isSoulSandPlant(mat)) {
            return findSoilBelow(plant, Material.SOUL_SAND);
        }
        return null;
    }

    public static boolean isSoilPlant(Material mat) {
        return mat.equals(Material.WHEAT)
            || mat.equals(Material.MELON_STEM)
            || mat.equals(Material.PUMPKIN_STEM)
            || mat.equals(Material.CARROT)
            || mat.equals(Material.POTATO)
            || mat.equals(Material.CROPS);
    }

    public static boolean isDirtPlant(Material mat) {
        return mat.equals(Material.SUGAR_CANE_BLOCK)
            || mat.equals(Material.MELON_BLOCK)
            || mat.equals(Material.PUMPKIN);
    }

    public static boolean isSandPlant(Material mat) {
        return mat.equals(Material.CACTUS);
    }

    public static boolean isSoulSandPlant(Material mat) {
        return mat.equals(Material.NETHER_WARTS);
    }

    public static boolean isPlant(Block plant) {
        Material mat = plant.getType();
        return isSoilPlant(mat)
            || isDirtPlant(mat)
            || isSandPlant(mat)
            || isSoulSandPlant(mat);
    }

    public static int maxPlantHeight(Block plant) {
        switch(plant.getType()) {
            case CACTUS:
            case SUGAR_CANE_BLOCK:
                return 3;
            default:
                return 1;
        }
    }

    public static Block findSoilBelow(Block plant, Material desired_type) {
        Block down = plant;
        int max_depth = maxPlantHeight(plant);
        for (int i = 0; i < max_depth; ++i) {
            down = down.getRelative(BlockFace.DOWN);
            if (down.getType().equals(desired_type)) {
                return down;
            }
        }
        return null;
    }

    @Override
    protected boolean shouldDelegate() {
        if (!isPlant(block)) {
            return false;
        }
        soil = findPlantSoil(block);
        if (soil == null) {
            return false;
        }
        reinforcement = Citadel.getReinforcementManager().getReinforcement(block);
        return reinforcement == null;
    }

    @Override
    protected void delegate() {
        reinforcement = null;
        block = soil;
    }
}
