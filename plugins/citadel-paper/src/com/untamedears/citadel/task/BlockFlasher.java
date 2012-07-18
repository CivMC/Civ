package com.untamedears.citadel.task;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.ContainerBlock;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.untamedears.citadel.Citadel;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/25/12
 * Time: 3:10 AM
 */
public class BlockFlasher implements Runnable {
    private Block block;
    private MaterialData flashData;
    private BlockState originalState;
    
    private BlockFlasher parent;
    private Runnable cleanupTask;

    public BlockFlasher(Block block, MaterialData flashData) {
        this.block = block;
        this.flashData = flashData;
        originalState = block.getState();
    }
    
    public BlockFlasher(BlockFlasher parent, MaterialData flashData) {
        this(parent.block, flashData);
        this.parent = parent;
    }

    public BlockFlasher chain(MaterialData flashData) {
        return new BlockFlasher(this, flashData);
    }
  
    public void start(Citadel plugin) {
        setCleanupTask();
        setData(flashData);

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, 20L * Citadel.getConfigManager().getFlashLength());
    }

    public void run() {
        if (parent == null) {
            revert();
        } else {
            parent.start(Citadel.getPlugin());
        }
    }

    private void setData(MaterialData data) {
        block.setTypeIdAndData(data.getItemTypeId(), data.getData(), true);
    }

    private void revert() {
        setData(originalState.getData());
        if (cleanupTask != null)
            cleanupTask.run();
        block.getState().update(true);
    }

    private void setCleanupTask() {
        if (originalState instanceof ContainerBlock) {
            final Inventory inventory = ((ContainerBlock) originalState).getInventory();
            final ItemStack[] contents = inventory.getContents();
            cleanupTask = new Runnable() {
                public void run() {
                    inventory.setContents(contents);
                }
            };
            inventory.clear();
        }
    }
}
