package com.programmerdan.minecraft.civspy.listeners.impl;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

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
	
	/**
	 * Generates: <code>player_movement</code> stat_key data. What state the player is in
	 * or the vehicle they are riding is stored as the string value field. The distance
	 * recorded, assuming the player moves more then a single block in any cardinal direction, is
	 * the number value field.
	 * 
	 * @param event The PlayerMoveEvent to record.
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void MovementListen(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		UUID id = p.getUniqueId();
		Location to = event.getTo();
		doMove(p, id, to);
	}
	
	private void doMove(Player p, UUID id, Location to) {
		try {
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
					p.isInsideVehicle() ? (p.getVehicle() == null ? "vehicle" : p.getVehicle().getType().toString()) : p.isSprinting() ? "running" : "walking";
				Chunk chunk = to.getChunk();
				DataSample chunkMovement = new PointDataSample("player.movement", this.getServer(), world, id,
						chunk.getX(), chunk.getZ(), type, distance);
				this.record(chunkMovement);
				storedLocations.put(id, to);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to spy a move event", e);
		}
	}
	
	/**
	 * Captures movement events for vehicles, too!
	 * @see #MovementListen(PlayerMoveEvent)
	 * 
	 * @param event
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void VehicleMovementListen(VehicleMoveEvent event) {
		for (Entity e : event.getVehicle().getPassengers()) {
			if (e instanceof Player) {
				Player p = (Player) e;
				UUID id = p.getUniqueId();
				Location to = event.getTo();
				doMove(p, id, to);
			}
		}
	}

	/**
	 * Just cleanly resets location to prevent weird location movement changes during teleports.
	 * 
	 * @param event
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void TeleportReset(PlayerTeleportEvent event) {
		Player p = event.getPlayer();
		Location to = event.getTo();
		UUID id = p.getUniqueId();
		storedLocations.put(id, to);
	}
	
}
