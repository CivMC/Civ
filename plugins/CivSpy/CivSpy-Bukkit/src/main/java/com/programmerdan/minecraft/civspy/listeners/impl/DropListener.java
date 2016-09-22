package com.programmerdan.minecraft.civspy.listeners.impl;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PointDataSample;
import com.programmerdan.minecraft.civspy.listeners.ServerDataListener;
import com.programmerdan.minecraft.civspy.util.ItemStackToString;

/**
 * Contributes <code>player.drop</code> stats when a person drops something
 * <br><br>
 * Contributes <code>block.dispense.TYPE</code> when a dispensor/dropper launches an item. 
 * TYPE is the material type of the block that is dispensed.
 * 
 * @author ProgrammerDan
 *
 */
public class DropListener extends ServerDataListener {

	public DropListener(DataManager target, Logger logger, String server) {
		super(target, logger, server);
	}

	@Override
	public void shutdown() {
		// no-op
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void itemDropListener(PlayerDropItemEvent event) {
		try {
			Player dropper = event.getPlayer();
			if (dropper == null) return;
			UUID id = dropper.getUniqueId();
			Item toDrop = event.getItemDrop();
			if (toDrop == null) return;
			
			Location location = toDrop.getLocation();
			if (location == null) return;
			Chunk chunk = location.getChunk();
			
			ItemStack drop = toDrop.getItemStack();
			if (drop == null) return;
			ItemStack dropQ = drop.clone();
			dropQ.setAmount(1);
			DataSample rdrop = new PointDataSample("player.drop", this.getServer(),
					chunk.getWorld().getName(), id, chunk.getX(), chunk.getZ(), 
					ItemStackToString.toString(dropQ), drop.getAmount());
			this.record(rdrop);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to spy a player drop event", e);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void dispensorListener(BlockDispenseEvent event) {
		try {
			Block block = event.getBlock();
			if (block == null) return;
			Chunk chunk = block.getChunk();
			
			ItemStack drop = event.getItem();
			if (drop == null) return;
			ItemStack dropQ = drop.clone();
			dropQ.setAmount(1);
			DataSample rdrop = new PointDataSample("block.dispense." + block.getType().toString(),
					this.getServer(), chunk.getWorld().getName(), null, chunk.getX(), chunk.getZ(), 
					ItemStackToString.toString(dropQ), drop.getAmount());
			this.record(rdrop);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to spy a dispense event", e);
		}
	}
}
