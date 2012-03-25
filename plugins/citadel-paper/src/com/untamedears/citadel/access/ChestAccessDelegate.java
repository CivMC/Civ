package com.untamedears.citadel.access;

import com.untamedears.citadel.Utility;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;
/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/23/12
 * Time: 3:27 PM
 */
public class ChestAccessDelegate extends AccessDelegate<MaterialData> {

    private Block attachedChest;
    
    public ChestAccessDelegate(Block block, MaterialData data) {
        super(block, data);
    }
    
    @Override
    protected boolean shouldDelegate() {
        reinforcement = plugin.dao.findReinforcement(block);
        return reinforcement == null && (attachedChest = Utility.getAttachedChest(block)) != null;
    }

    @Override
    protected void delegate() {
        reinforcement = null;
        block = attachedChest;
    }
}
