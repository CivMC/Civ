package com.programmerdan.minecraft.civspy.listeners.impl;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PointDataSample;
import com.programmerdan.minecraft.civspy.listeners.ServerDataListener;
import com.programmerdan.minecraft.civspy.util.ItemStackToString;
import com.programmerdan.minecraft.cropcontrol.events.CropControlDropEvent;

/**
 * Contributes <code>cropcontrol.drop.TYPE</code> stats when crop control triggers a drop.
 * Covers all types of drops including player sponsored. Records which item(s) were dropped in the string
 * value field. TYPE is the kind of break -- check the crop control documentation for which values are valid.
 * 
 * @author ProgrammerDan
 *
 */
public class CropControlListener extends ServerDataListener {

	public CropControlListener(DataManager target, Logger logger, String server) {
		super(target, logger, server);
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void cropDropEvent(CropControlDropEvent event) {
		try {
			UUID id = event.getPlayer();
			
			List<ItemStack> toDrop = event.getItems();
			if (toDrop == null) return;
			
			Location location = event.getLocation();
			if (location == null) return;
			
			Chunk chunk = location.getChunk();
			
			for (ItemStack drop : toDrop) {
				if (drop == null) continue;
				ItemStack dropQ = drop.clone();
				dropQ.setAmount(1);
				DataSample rdrop = new PointDataSample("cropcontrol.drop." + event.getBreakType().toString(),
						this.getServer(), chunk.getWorld().getName(), id, chunk.getX(), chunk.getZ(), 
						ItemStackToString.toString(dropQ), drop.getAmount());
				this.record(rdrop);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to spy a cropcontrol drop event", e);
		}
	}

	@Override
	public void shutdown() {
		// NO-OP
	}

}
