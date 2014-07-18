package com.untamedears.citadel.access;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;
/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/23/12
 * Time: 3:26 PM
 */
public class DoorAccessDelegate extends AccessDelegate<Door> {
    public static boolean canDelegate(Block block, MaterialData data) {
        return (data instanceof Door);
    }

    public DoorAccessDelegate(Block block, Door data) {
        super(block, data);
    }

    @Override
    protected boolean shouldDelegate() {
        return data.isTopHalf();
    }

    @Override
    protected void delegate() {
        block = block.getRelative(BlockFace.DOWN);
    }
}
