package com.untamedears.citadel.access;

import static com.untamedears.citadel.Utility.findPlantSoil;
import static com.untamedears.citadel.Utility.isPlant;

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
