package com.programmerdan.minecraft.civspy.listeners.impl;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PointDataSample;
import com.programmerdan.minecraft.civspy.listeners.ServerDataListener;
import com.programmerdan.minecraft.civspy.util.ItemStackToString;

public class ConsumeListener extends ServerDataListener {

	public ConsumeListener(DataManager target, Logger logger, String server) {
		super(target, logger, server);
	}

	@Override
	public void shutdown() {
		// NO-OP
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void consumeEvent(PlayerItemConsumeEvent consumeEvent) {
		try {
			Player player = consumeEvent.getPlayer();
			if (player == null) return;
			UUID uuid = player.getUniqueId();
			
			ItemStack inItem = consumeEvent.getItem();
			if (inItem == null) return;
			
			Location location = player.getLocation();
			if (location == null) return;
			Chunk chunk = location.getChunk();
		
			ItemStack pickQ = inItem.clone();
			pickQ.setAmount(1);
			DataSample rpick = new PointDataSample("player.consume", this.getServer(),
					chunk.getWorld().getName(), uuid, chunk.getX(), chunk.getZ(), 
					ItemStackToString.toString(pickQ), inItem.getAmount());
			this.record(rpick);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to track Consume Event in CivSpy", e);
		}
	}
}
