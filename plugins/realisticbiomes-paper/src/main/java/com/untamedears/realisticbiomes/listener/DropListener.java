package com.untamedears.realisticbiomes.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;

import com.untamedears.realisticbiomes.BlockDropManager;
import com.untamedears.realisticbiomes.model.gauss.drop.BlockDrop;

import vg.civcraft.mc.civmodcore.util.DelayedItemDrop;

public class DropListener implements Listener {

	private BlockDropManager dropManager;
	
	public DropListener(BlockDropManager dropManager) {
		this.dropManager = dropManager;
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBreak(BlockDropItemEvent e) {
		BlockDrop drop = dropManager.getAndRemove(e.getBlock().getLocation());
		if (drop != null) {
			e.setCancelled(true);
			DelayedItemDrop.dropAt(e.getBlock().getLocation(), drop.getDrops());
		}
	}
}
