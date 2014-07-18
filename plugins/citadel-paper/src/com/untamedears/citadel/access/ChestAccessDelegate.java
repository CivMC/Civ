package com.untamedears.citadel.access;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.Utility;
/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/23/12
 * Time: 3:27 PM
 */
public class ChestAccessDelegate extends AccessDelegate<MaterialData> {
    public static boolean canDelegate(Block block, MaterialData data) {
        final Material mat = block.getType();
        return mat.equals(Material.CHEST) || mat.equals(Material.TRAPPED_CHEST);
    }

    private Block attachedChest;
    
    public ChestAccessDelegate(Block block, MaterialData data) {
        super(block, data);
    }
    
    @Override
    protected boolean shouldDelegate() {
        reinforcement = Citadel.getReinforcementManager().getReinforcement(block);
        return reinforcement == null && (attachedChest = Utility.getAttachedChest(block)) != null;
    }

    @Override
    protected void delegate() {
        reinforcement = null;
        block = attachedChest;
    }
}
