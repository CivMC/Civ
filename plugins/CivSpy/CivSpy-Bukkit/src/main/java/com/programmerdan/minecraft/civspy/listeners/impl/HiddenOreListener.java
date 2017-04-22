package com.programmerdan.minecraft.civspy.listeners.impl;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

import com.github.devotedmc.hiddenore.events.HiddenOreEvent;
import com.github.devotedmc.hiddenore.events.HiddenOreGenerateEvent;
import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PointDataSample;
import com.programmerdan.minecraft.civspy.listeners.ServerDataListener;
import com.programmerdan.minecraft.civspy.util.ItemStackToString;

public class HiddenOreListener extends ServerDataListener {

	public HiddenOreListener(DataManager target, Logger logger, String server) {
		super(target, logger, server);
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void oreDropEvent(HiddenOreEvent event) {
		try {
			Player dropper = event.getPlayer();
			
			UUID id = dropper != null ? dropper.getUniqueId() : null;
			
			List<ItemStack> toDrop = event.getDrops();
			if (toDrop == null) return;
			
			Location location = event.getDropLocation();
			if (location == null) return;
			
			Chunk chunk = location.getChunk();
			
			for (ItemStack drop : toDrop) {
				if (drop == null) continue;
				ItemStack dropQ = drop.clone();
				dropQ.setAmount(1);
				DataSample rdrop = new PointDataSample("hiddenore.drop", this.getServer(),
						chunk.getWorld().getName(), id, chunk.getX(), chunk.getZ(), 
						ItemStackToString.toString(dropQ), drop.getAmount());
				this.record(rdrop);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to spy a hiddenore drop event", e);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void oreGenerateEvent(HiddenOreGenerateEvent event) {
		try {
			Player genner = event.getPlayer();
			
			UUID id = genner != null ? genner.getUniqueId() : null;
			
			Material toGen = event.getTransform();
			if (toGen == null) return;
			
			Block preGen = event.getBlock();
			if (preGen == null) return;
			Material fromGen = preGen.getType();
			
			Location location = preGen.getLocation();
			if (location == null) return;
			
			Chunk chunk = location.getChunk();
			
			DataSample rgen = new PointDataSample("hiddenore.gen", this.getServer(),
						chunk.getWorld().getName(), id, chunk.getX(), chunk.getZ(), 
						toGen.toString());
			this.record(rgen);
			
			DataSample rrep = new PointDataSample("hiddenore.replace", this.getServer(),
					chunk.getWorld().getName(), id, chunk.getX(), chunk.getZ(), 
					fromGen.toString());
			this.record(rrep);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to spy a hiddenore generate event", e);
		}
	}

	@Override
	public void shutdown() {
		// NO-OP
	}

}
