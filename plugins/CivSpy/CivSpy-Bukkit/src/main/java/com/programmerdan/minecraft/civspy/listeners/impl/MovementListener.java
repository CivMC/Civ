package com.programmerdan.minecraft.civspy.listeners.impl;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

import com.programmerdan.minecraft.civspy.DataListener;
import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PointDataSample;
import com.programmerdan.minecraft.civspy.listeners.ServerDataListener;

/**
 * Example Listener; this sends for aggregation movement data on players. It records distance travelled at chunk granularities within 
 *  the global sampling period rate.
 * 
 * @author ProgrammerDan
 */
public final class MovementListener extends ServerDataListener {

	private ConcurrentHashMap<UUID, Location> storedLocations;
	
	public MovementListener(DataManager target, Logger logger, String server) {
		super(target, logger, server);
		this.storedLocations = new ConcurrentHashMap<UUID, Location>();
	}

	@Override
	public void shutdown() {
		this.storedLocations.clear();
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void MovementListen(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		UUID id = p.getUniqueId();
		Location to = event.getTo();
		Location from = storedLocations.get(id);
		if (from == null || !from.getWorld().equals(to.getWorld())) {
			storedLocations.put(id, to);
			return;
		}
		
		// Check if the player moved at least 1 in any cardinal axis.
		int xMove = from.getBlockX() - to.getBlockX();
		int yMove = from.getBlockY() - to.getBlockY();
		int zMove = from.getBlockZ() - to.getBlockZ();
		if (xMove != 0 || yMove != 0 || zMove != 0) {
			String world = to.getWorld().getName();
			double distance = Math.sqrt(xMove * xMove + yMove * yMove + zMove * zMove);
			String type = p.isSneaking() ? "sneaking" : p.isFlying() ? "flying" : p.isGliding() ? "gliding" : 
				p.isInsideVehicle() ? "vehicle" : p.isSprinting() ? "running" : "walking";
			Chunk chunk = to.getChunk();
			DataSample chunkMovement = new PointDataSample("player.movement", this.getServer(), world, id,
					chunk.getX(), chunk.getZ(), type, distance);
			this.record(chunkMovement);
			storedLocations.put(id, to);
		}
	}

}
