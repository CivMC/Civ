package com.programmerdan.minecraft.civspy.listeners.impl;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PointDataSample;
import com.programmerdan.minecraft.civspy.listeners.ServerDataListener;
import com.programmerdan.minecraft.civspy.util.ItemStackToString;

/**
 * Sample Listener class that records all block breaks for summation by who and what.
 * This only records _player_ breaks, so if other entities cause a break those events are
 * ignored.
 * 
 * @author ProgrammerDan
 */
public final class BreakListener extends ServerDataListener {

	public BreakListener(DataManager target, Logger logger, String server) {
		super(target, logger, server);
	}
	
	@Override
	public void shutdown() {
		// NO-OP
	}
	
	/**
	 * Generates: <code>player.blockbreak</code> stat_key data. Block encoded attributes
	 * is stored in the string value field.
	 * <br><br>
	 * Generates: <code>block.drop.TYPE</code> when the block broken drops items. 
	 * TYPE is the material type of the block that is dropping stuff.
	 * 
	 * @param event The BlockBreakEvent
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void BreakListen(BlockBreakEvent event) {
		try {
			Player p = event.getPlayer();
			if (p == null) return;
			UUID id = p.getUniqueId();
			Block broken = event.getBlock();
			Chunk chunk = broken.getChunk();
			
			if (broken instanceof InventoryHolder) {
				Inventory inventory = ((InventoryHolder) broken).getInventory();
				ItemStack[] dropped = inventory.getStorageContents();
				
				if (dropped != null && dropped.length > 0) {
					for (ItemStack drop : dropped) {
						ItemStack dropQ = drop.clone();
						dropQ.setAmount(1);
						DataSample deathdrop = new PointDataSample("block.drop." + broken.getType().toString(),
								this.getServer(), chunk.getWorld().getName(), id, chunk.getX(), chunk.getZ(), 
								ItemStackToString.toString(dropQ), drop.getAmount());
						this.record(deathdrop);
					}
				}
			}
			
			DataSample blockBreak = new PointDataSample("player.blockbreak", this.getServer(),
					chunk.getWorld().getName(), id, chunk.getX(), chunk.getZ(), 
					ItemStackToString.toString(broken.getState()));
			this.record(blockBreak);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to spy a break event", e);
		}
	}

}
