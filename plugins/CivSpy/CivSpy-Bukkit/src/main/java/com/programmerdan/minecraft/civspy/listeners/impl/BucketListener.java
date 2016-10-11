package com.programmerdan.minecraft.civspy.listeners.impl;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.ItemStack;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PointDataSample;
import com.programmerdan.minecraft.civspy.listeners.ServerDataListener;
import com.programmerdan.minecraft.civspy.util.ItemStackToString;

/**
 * <code>player.bucket.empty</code> and <code>player.bucket.fill</code> 
 * 
 * includes player and nature of bucket
 * 
 * @author ProgrammerDan
 *
 */
public class BucketListener extends ServerDataListener {

	public BucketListener(DataManager target, Logger logger, String server) {
		super(target, logger, server);
	}

	@Override
	public void shutdown() {
		// NO-OP
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void consumeEvent(PlayerBucketEmptyEvent emptyEvent) {
		try {
			Player player = emptyEvent.getPlayer();
			if (player == null) return;
			UUID uuid = player.getUniqueId();

			Location location = null;
			
			Block onBlock = emptyEvent.getBlockClicked();
			if (onBlock != null) {
				Block fillBlock = onBlock.getRelative(emptyEvent.getBlockFace());
				location = fillBlock.getLocation();
			}
			
			if (location == null) {
				location = player.getLocation();
			}
			
			if (location == null) return;
			Chunk chunk = location.getChunk();
			
			ItemStack bucket = emptyEvent.getItemStack();
			ItemStack pickQ = bucket.clone();
			pickQ.setAmount(1);
		
			DataSample rpick = new PointDataSample("player.bucket.empty", this.getServer(),
					chunk.getWorld().getName(), uuid, chunk.getX(), chunk.getZ(), 
					ItemStackToString.toString(pickQ), bucket.getAmount());
			this.record(rpick);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to track Bucket Empty Event in CivSpy", e);
		}
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void fillEvent(PlayerBucketFillEvent fillEvent) {
		try {
			Player player = fillEvent.getPlayer();
			if (player == null) return;
			UUID uuid = player.getUniqueId();

			Location location = null;
			
			Block onBlock = fillEvent.getBlockClicked();
			if (onBlock != null) {
				Block fillBlock = onBlock.getRelative(fillEvent.getBlockFace());
				location = fillBlock.getLocation();
			}
			
			if (location == null) {
				location = player.getLocation();
			}
			
			if (location == null) return;
			Chunk chunk = location.getChunk();
			
			ItemStack bucket = fillEvent.getItemStack();
			ItemStack pickQ = bucket.clone();
			pickQ.setAmount(1);
		
			DataSample rpick = new PointDataSample("player.bucket.fill", this.getServer(),
					chunk.getWorld().getName(), uuid, chunk.getX(), chunk.getZ(), 
					ItemStackToString.toString(pickQ), bucket.getAmount());
			this.record(rpick);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to track Bucket Fill Event in CivSpy", e);
		}
	}
}
