package com.untamedears.citadel.access;

import static com.untamedears.citadel.Utility.isPlant;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.material.Bed;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.Citadel.VerboseMsg;
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
        if (DoorAccessDelegate.canDelegate(block, data)) {
            return new DoorAccessDelegate(block, (Door) data);

        } else if (BedAccessDelegate.canDelegate(block, data)) {
            return new BedAccessDelegate(block, (Bed) data);

        } else if (ChestAccessDelegate.canDelegate(block, data)) {
            return new ChestAccessDelegate(block, data);

        } else if (CropAccessDelegate.canDelegate(block, data)) {
            return new CropAccessDelegate(block, data);
        }
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
    
    protected Block block;
    protected T data;
    protected IReinforcement reinforcement;

    public AccessDelegate(Block block, T data) {
        this.block = block;
        this.data = data;

        boolean show_info = !(this instanceof CropAccessDelegate);
        if (shouldDelegate()) {
            if (show_info) {
                Citadel.verbose(
                    VerboseMsg.InteractionAttempt,
                    block.getLocation().toString());
            }
            delegate();
            if (show_info) {
                Citadel.verbose(
                    VerboseMsg.ReinDelegation,
                    block.getLocation().toString());
            }
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

    public boolean isReinforced() {
        return getReinforcement() != null;
    }
}
