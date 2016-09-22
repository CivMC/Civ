package com.programmerdan.minecraft.civspy.listeners.impl;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PointDataSample;
import com.programmerdan.minecraft.civspy.listeners.ServerDataListener;
import com.programmerdan.minecraft.civspy.util.ItemStackToString;

/**
 * Sample Listener class that records all block placements for summation by who and what.
 * This only records _player_ placements, so if other entities cause a placement those events are
 * ignored.
 * 
 * @author ProgrammerDan
 */
public final class PlaceListener extends ServerDataListener {

	public PlaceListener(DataManager target, Logger logger, String server) {
		super(target, logger, server);
	}
	
	@Override
	public void shutdown() {
		// NO-OP
	}
	
	/**
	 * Generates: <code>player.blockbreak</code> stat_key data. Block serialization
	 * is stored in the string value field.
	 * 
	 * @param event The BlockBreakEvent
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void PlaceListen(BlockPlaceEvent event) {
		try {
			Player p = event.getPlayer();
			if (p == null) return;
			UUID id = p.getUniqueId();
			Block placed = event.getBlockPlaced();
			Chunk chunk = placed.getChunk();
		
			DataSample blockPlace = new PointDataSample("player.blockplace", this.getServer(),
					chunk.getWorld().getName(), id, chunk.getX(), chunk.getZ(), 
					ItemStackToString.toString(placed.getState()));
			this.record(blockPlace);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to spy a place event", e);
		}
	}

}
