package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.InvisibleFixTwoConfig;

import net.minecraft.server.v1_12_R1.EntityTracker;
import net.minecraft.server.v1_12_R1.EntityTrackerEntry;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_12_R1.WorldServer;

/**
 * DISABLE WITH 1.11 -- This is for 1.10 only. Basically doing what Mojang was too
 * busy / lazy to do before 1.11. Note this is not a perfect fix, but at least
 * seems to deal with the largest areas of issue, namely, boats and horses,
 * by forcing a location packet to be broadcast to other players that are tracking.
 * 
 * Portions adapted and heavily modified from code by aadnk: https://gist.github.com/aadnk/3773860
 * 
 * @author ProgrammerDan
 */
public class InvisibleFixTwo extends SimpleHack<InvisibleFixTwoConfig> implements Listener {
	public static final String NAME = "InvisibleFixTwo";

	private Server server;
	private Map<Integer, Long> updateMap;

	public InvisibleFixTwo(SimpleAdminHacks plugin, InvisibleFixTwoConfig config) {
		super(plugin, config);
		this.server = plugin.getServer();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (!config.isEnabled()) return; // ignore if off

		final Player player = event.getPlayer();

		if (player.isOp() && config.getIgnoreOps()) return;
		String tPerm = config.getIgnorePermission();
		if (tPerm != null && player.hasPermission(tPerm)) return;

		server.getScheduler().runTaskLater(plugin(), new Runnable() {
			@Override
			public void run() {
				try {
					forceUpdate(player);
				} catch (NullPointerException npe) {
					plugin().debug("Player offline, no forcefix");
				}
			}
		}, config.getTeleportFixDelay());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerVehicleMove(VehicleMoveEvent move) {
		if (!config.isEnabled()) return; // ignore if off

		Vehicle vehicle = move.getVehicle();
		if (vehicle == null) return;
		if (vehicle.getPassengers() == null || vehicle.getPassengers().size() == 0) return;

		for (Entity e : vehicle.getPassengers()) {
			if (e == null) continue;

			if (e.isOp() && config.getIgnoreOps()) continue;
			String tPerm = config.getIgnorePermission();
			if (tPerm != null && e.hasPermission(tPerm)) continue;

			forceUpdate(e);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMoveInVehicle(PlayerMoveEvent move) {
		if (!config.isEnabled()) return; // ignore if off

		Player p = move.getPlayer();
		if (p == null) return;

		if (p.isOp() && config.getIgnoreOps()) return;
		String tPerm = config.getIgnorePermission();
		if (tPerm != null && p.hasPermission(tPerm)) return;

		Entity vehicle = p.getVehicle();
		if (vehicle == null) return;
		forceUpdate(p);
	}

	private void forceUpdate(Entity entity) {
		Long last = updateMap.get(entity.getEntityId());
		long now = System.currentTimeMillis();
		if (last == null || last < now - config.getFixInterval()) {
			updateEntities(entity);
			updateMap.put(entity.getEntityId(), now);
			//plugin().debug("Forcing packet-based update for {0}", entity);
		}
	}

	private void updateEntities(final Entity entity) {
		World world = entity.getWorld();
		WorldServer worldServer = ((CraftWorld) world).getHandle();

		EntityTracker tracker = worldServer.tracker;

		EntityTrackerEntry entry = (EntityTrackerEntry) tracker.trackedEntities
				.get(entity.getEntityId());

		net.minecraft.server.v1_12_R1.Entity eEntity = getNmsEntity(entity);

		entry.broadcast(new PacketPlayOutEntityTeleport(eEntity));
	}

	private net.minecraft.server.v1_12_R1.Entity getNmsEntity(final Entity entity) {
		CraftEntity craftEntity = (CraftEntity) entity;
		return craftEntity.getHandle();
	}

	@Override
	public void registerListeners() {
		if (config.isEnabled()) {
			plugin().log("Registering InvisibleFixTwo listener");
			plugin().registerListener(this);
		}
	}

	@Override
	public void registerCommands() {
	}

	@Override
	public void dataBootstrap() {
		updateMap = new HashMap<Integer, Long>();
	}

	@Override
	public void unregisterListeners() {
	}

	@Override
	public void unregisterCommands() {
	}

	@Override
	public void dataCleanup() {
		if (updateMap != null) {
			updateMap.clear();
		}
	}

	@Override
	public String status() {
		if (config != null && config.isEnabled()) {
			return "InvisibleFixTwo active";
		} else {
			return "InvisibleFixTwo not active";
		}
	}

	public static InvisibleFixTwoConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new InvisibleFixTwoConfig(plugin, config);
	}
}