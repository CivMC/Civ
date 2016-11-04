package com.programmerdan.minecraft.civspy.listeners.impl;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockRedstoneEvent;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PointDataSample;
import com.programmerdan.minecraft.civspy.listeners.ServerDataListener;
import com.programmerdan.minecraft.civspy.util.ItemStackToString;

/**
 * Contributes the <code>redstone.current.increase</code> and .decrease and .stable datamarkers.
 *
 * String value is the block that had a change in current.
 *
 * @author ProgrammerDan
 */
public class RedstoneListener extends ServerDataListener {

	public RedstoneListener(DataManager target, Logger logger, String server) {
		super(target, logger, server);
	}

	@Override
	public void shutdown() {
		// NO-OP
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void redstoneEvent(BlockRedstoneEvent event) {
		try {
			Block block = event.getBlock();
			if (block == null) return;
			
			BlockState state = block.getState();
			if (state == null) return;
			
			int oldCurrent = event.getOldCurrent();
			int newCurrent = event.getNewCurrent();


			Location location = block.getLocation();
			if (location == null) return;
			Chunk chunk = location.getChunk();
		
			String sname = "redstone.current.";
			if (newCurrent > oldCurrent) {
				sname += "increase";
			} else if (newCurrent == oldCurrent) {
				sname += "stable";
			} else {
				sname += "decrease";
			}

			DataSample rstone = new PointDataSample(sname, this.getServer(),
					chunk.getWorld().getName(), null, chunk.getX(), chunk.getZ(), 
					ItemStackToString.toString(state));
			this.record(rstone);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to track Redstone Event in CivSpy", e);
		}
	}
}

