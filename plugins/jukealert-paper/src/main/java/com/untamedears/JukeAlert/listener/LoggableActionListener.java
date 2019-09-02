package com.untamedears.JukeAlert.listener;

import java.util.Collection;
import java.util.function.Supplier;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import com.untamedears.JukeAlert.SnitchManager;
import com.untamedears.JukeAlert.external.VanishNoPacket;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.actions.LoggedSnitchAction;
import com.untamedears.JukeAlert.model.actions.impl.EntryAction;
import com.untamedears.JukeAlert.model.actions.impl.LoginAction;
import com.untamedears.JukeAlert.model.actions.impl.LogoutAction;
import com.untamedears.JukeAlert.util.JukeAlertPermissionHandler;

public class LoggableActionListener implements Listener {
	
	private final VanishNoPacket vanishNoPacket;
	private final SnitchManager snitchManager;
	
	public LoggableActionListener(SnitchManager snitchManager) {
		this.snitchManager = snitchManager;
		this.vanishNoPacket = new VanishNoPacket();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void enterSnitchProximity(PlayerMoveEvent event) {
		Location from = event.getFrom();
		Location to = event.getTo();
		if (to == null) {
			return;
		}

		if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY()
				&& from.getBlockZ() == to.getBlockZ() && from.getWorld().equals(to.getWorld())) {
			// Player didn't move by at least one block
			return;
		}
		handleSnitchEntry(event.getPlayer(), to);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		handleSnitchEntry(event.getPlayer(), event.getTo());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onVehicleMovement(VehicleMoveEvent event) {
		for (Entity e : event.getVehicle().getPassengers()) {
			if (e instanceof Player) {
				enterSnitchProximity(new PlayerMoveEvent((Player) e, event.getFrom(), event.getTo()));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void playerJoinEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		handlePlayerAction(player, () -> new LoginAction(System.currentTimeMillis(), player.getUniqueId()));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerQuitEvent(PlayerQuitEvent event) {
		handleSnitchLogout(event.getPlayer());
	}
	
	public void playerKickEvent(PlayerKickEvent event) {
		//TODO Old JA had this listener, is it really needed?
		handleSnitchLogout(event.getPlayer());
	}

	private void handleSnitchLogout(Player player) {
		handlePlayerAction(player, () -> new LogoutAction(System.currentTimeMillis(), player.getUniqueId()));
	}

	private void handlePlayerAction(Player player, Supplier<LoggedSnitchAction> actionCreator) {
		if (isPlayerSnitchImmune(player)) {
			return;
		}
		Collection<Snitch> snitches = snitchManager.getSnitchesCovering(player.getLocation());
		for (Snitch snitch : snitches) {
			if (!snitch.hasPermission(player, JukeAlertPermissionHandler.getSnitchImmune())) {
				snitch.getLoggingDelegate().addAction(actionCreator.get());
			}
		}
	}

	private void handleSnitchEntry(Player player, Location location) {
		if (isPlayerSnitchImmune(player)) {
			return;
		}
		Collection<Snitch> snitches = snitchManager.getSnitchesCovering(location);
		for (Snitch snitch : snitches) {
			if (!snitch.hasPermission(player, JukeAlertPermissionHandler.getSnitchImmune())) {
				snitch.getLoggingDelegate()
						.addAction(new EntryAction(System.currentTimeMillis(), player.getUniqueId()));
			}
			if (snitch.hasPermission(player, JukeAlertPermissionHandler.getListSnitches())) {
				snitch.refresh();
			}
		}
	}

	private boolean isPlayerSnitchImmune(Player player) {
		return vanishNoPacket.isPlayerInvisible(player) || player.hasPermission("jukealert.vanish");
	}

}
