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
import org.bukkit.inventory.ItemStack;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PointDataSample;
import com.programmerdan.minecraft.civspy.listeners.ServerDataListener;
import com.programmerdan.minecraft.civspy.util.ItemStackToString;

/**
 * Contributes player.fishing tracking data, records location, player, and item / amount retrieved.
 * 
 * @author ProgrammerDan
 *
 */
public class FishingListener extends ServerDataListener {

    public FishingListener(DataManager target, Logger logger, String server) {
		super(target, logger, server);
	}

	@Override
    public void shutdown() {
        // NO-OP
    }

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void fishingEvent(PlayerFishEvent fishEvent) {
		try {
			Player player = fishEvent.getPlayer();
			State state = fishEvent.getState();
			Entity caught = fishEvent.getCaught();
			
			if (!State.CAUGHT_FISH.equals(state) || player == null || caught == null) return;
			UUID uuid = player.getUniqueId();
				
			if (!(caught instanceof Item)) return;
			Item item = (Item) caught;
			ItemStack inItem = item.getItemStack();
			if (inItem == null) return;
			
			Location location = item.getLocation();
			if (location == null) return;
			Chunk chunk = location.getChunk();
		
			ItemStack pickQ = inItem.clone();
			pickQ.setAmount(1);
			DataSample rpick = new PointDataSample("player.fishing", this.getServer(),
					chunk.getWorld().getName(), uuid, chunk.getX(), chunk.getZ(), 
					ItemStackToString.toString(pickQ), inItem.getAmount());
			this.record(rpick);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to track Fishing Event in CivSpy", e);
		}
	}
}
