package com.untamedears.JukeAlert.listener;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;

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
import com.untamedears.JukeAlert.model.actions.SnitchAction;
import com.untamedears.JukeAlert.model.actions.impl.EnterFieldAction;
import com.untamedears.JukeAlert.model.actions.impl.LeaveFieldAction;
import com.untamedears.JukeAlert.model.actions.impl.LoginAction;
import com.untamedears.JukeAlert.model.actions.impl.LogoutAction;
import com.untamedears.JukeAlert.util.JukeAlertPermissionHandler;

public class LoggableActionListener implements Listener {

	private final VanishNoPacket vanishNoPacket;
	private final SnitchManager snitchManager;
	private final Map<UUID, Set<Snitch>> insideFields;

	public LoggableActionListener(SnitchManager snitchManager) {
		this.snitchManager = snitchManager;
		this.vanishNoPacket = new VanishNoPacket();
		this.insideFields = new TreeMap<>();
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
		handlePlayerAction(player, (s) -> new LoginAction(System.currentTimeMillis(), s, player.getUniqueId()));
		insideFields.put(event.getPlayer().getUniqueId(), 
				new HashSet<>(snitchManager.getSnitchesCovering(event.getPlayer().getLocation())));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerQuitEvent(PlayerQuitEvent event) {
		handleSnitchLogout(event.getPlayer());
	}

	public void playerKickEvent(PlayerKickEvent event) {
		// TODO Old JA had this listener, is it really needed?
		handleSnitchLogout(event.getPlayer());
	}

	private void handleSnitchLogout(Player player) {
		handlePlayerAction(player, (s) -> new LogoutAction(System.currentTimeMillis(), s, player.getUniqueId()));
	}

	private void handlePlayerAction(Player player, Function<Snitch, SnitchAction> actionCreator) {
		if (isPlayerSnitchImmune(player)) {
			return;
		}
		Collection<Snitch> snitches = snitchManager.getSnitchesCovering(player.getLocation());
		for (Snitch snitch : snitches) {
			if (!snitch.hasPermission(player, JukeAlertPermissionHandler.getSnitchImmune())) {
				snitch.processAction(actionCreator.apply(snitch));
			}
		}
	}

	private void handleSnitchEntry(Player player, Location location) {
		if (isPlayerSnitchImmune(player)) {
			return;
		}
		Collection<Snitch> insideNow = snitchManager.getSnitchesCovering(location);
		Set<Snitch> previouslyIn = insideFields.get(player.getUniqueId());
		insideNow.stream().filter(s -> !previouslyIn.contains(s)).forEach(s -> {
			s.processAction(new EnterFieldAction(System.currentTimeMillis(), s, player.getUniqueId()));
			previouslyIn.add(s);
		});
		List<Snitch> toRemove = new LinkedList<>();
		previouslyIn.stream().filter(s -> !insideNow.contains(s)).forEach(s -> {
			s.processAction(new LeaveFieldAction(System.currentTimeMillis(), s, player.getUniqueId()));
			toRemove.add(s);
		});
		//need to do this afterwards to avoid ConcurrentModificationExceptions
		previouslyIn.removeAll(toRemove);
	}

	private boolean isPlayerSnitchImmune(Player player) {
		return vanishNoPacket.isPlayerInvisible(player) || player.hasPermission("jukealert.vanish");
	}

}
