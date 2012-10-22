package com.untamedears.citadel.access;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.material.Bed;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.entity.IReinforcement;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/23/12
 * Time: 3:16 PM
 */
public abstract class AccessDelegate<T extends MaterialData> {

    public static AccessDelegate getDelegate(Block block) {
        MaterialData data = block.getState().getData();
        if (data instanceof Door) {
            return new DoorAccessDelegate(block, (Door) data);
        } else if (data instanceof Bed) {
            return new BedAccessDelegate(block, (Bed) data);
        } else if (block.getType() == Material.CHEST) {
            return new ChestAccessDelegate(block, data);
        } else {
            return new AccessDelegate<MaterialData>(block, data) {
                @Override
                protected boolean shouldDelegate() {
                    return false;
                }
                @Override
                protected void delegate() {
                }
            };
        }
    }
    
    protected Block block;
    protected T data;
    protected IReinforcement reinforcement;

    public AccessDelegate(Block block, T data) {
        this.block = block;
        this.data = data;

        if (shouldDelegate()) {
            Citadel.info("Attempted interaction with %s block at " + block.getLocation().toString());
            delegate();
            Citadel.info("Delegated to %s block at " + block.getLocation().toString());
        }
    }

    protected abstract boolean shouldDelegate();
    protected abstract void delegate();
    
    public Block getBlock() {
        return block;
    }
    
    public IReinforcement getReinforcement() {
        if (reinforcement == null) reinforcement = Citadel.getReinforcementManager().getReinforcement(block);
        return reinforcement;
    }
}
