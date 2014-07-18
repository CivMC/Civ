package com.untamedears.citadel.access;

import org.bukkit.block.Block;
import org.bukkit.block.Block;
import org.bukkit.material.Bed;
import org.bukkit.material.MaterialData;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/23/12
 * Time: 3:24 PM
 */
public class BedAccessDelegate extends AccessDelegate<Bed> {
    public static boolean canDelegate(Block block, MaterialData data) {
        return (data instanceof Bed);
    }

    public BedAccessDelegate(Block block, Bed data) {
        super(block, data);
    }

    @Override
    protected boolean shouldDelegate() {
        return data.isHeadOfBed();
    }

    @Override
    protected void delegate() {
        block = block.getRelative(data.getFacing().getOppositeFace());
    }
}
