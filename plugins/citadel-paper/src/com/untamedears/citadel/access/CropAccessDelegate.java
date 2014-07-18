package com.untamedears.citadel.access;

import static com.untamedears.citadel.Utility.findPlantSoil;
import static com.untamedears.citadel.Utility.isPlant;
import static com.untamedears.citadel.Utility.isReinforceablePlant;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.MaterialData;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.Utility;
import com.untamedears.citadel.entity.IReinforcement;

public class CropAccessDelegate extends AccessDelegate<MaterialData> {
    public static boolean canDelegate(Block block, MaterialData data) {
        if (!Citadel.getConfigManager().allowReinforcedCrops() || !isPlant(block)) {
            return false;
        }
        if (!isReinforceablePlant(block.getType())) {
            return true;
        }
        IReinforcement rein = Citadel.getReinforcementManager().getReinforcement(block);
        return rein == null;
    }

    private Block soil;

    public CropAccessDelegate(Block block, MaterialData data) {
        super(block, data);
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
