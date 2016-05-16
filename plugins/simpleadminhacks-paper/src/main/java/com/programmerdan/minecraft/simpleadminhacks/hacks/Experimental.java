package com.programmerdan.minecraft.simpleadminhacks.hacks;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import org.bukkit.entity.Player;
import org.bukkit.Location;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.ExperimentalConfig;

public class Experimental extends SimpleHack<ExperimentalConfig> implements Listener {

	public static final String NAME = "Experimental";
	
	public Experimental(SimpleAdminHacks plugin, ExperimentalConfig config) {
		super(plugin, config);
	}

	@Override
	public void registerListeners() {
		if (config.isEnabled()) {
			plugin().log("Registering experimental listeners");
			plugin().registerListener(this);
		}
	}

	@Override
	public void registerCommands() {
		if (config.isEnabled()) {
			plugin().log("Registering experimental commands");
		}
	}

	@Override
	public void dataBootstrap() {
	}

	@Override
	public void unregisterListeners() {
	}

	@Override
	public void unregisterCommands() {
	}

	@Override
	public void dataCleanup() {
	}

	@Override
	public String status() {
		if (config.isEnabled()) {
			StringBuilder sb = new StringBuilder("Experiments enabled:");
			if (config.isCombatSpy()) {
				sb.append("\n  CombatSpy is on");
			} else {
				sb.append("\n  CombatSpy is off");
			}
			return sb.toString();
		} else {
			return "Experiments disabled.";
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	private void monitorTeleportLow(PlayerTeleportEvent event) {
		StringBuffer sb = new StringBuffer("[LO] ");
		logTeleport(event, sb);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	private void monitorTeleportHigh(PlayerTeleportEvent event) {
		StringBuffer sb = new StringBuffer("[HI] ");
		logTeleport(event, sb);
	}

	private void logTeleport(PlayerTeleportEvent event, StringBuffer sb) {
		sb.append(event.isCancelled() ? "C " : "A ");
		sb.append(event.getCause().name());
		Player player = event.getPlayer();
		Location from = event.getFrom();
		Location to = event.getTo();
		sb.append(String.format(" %16s", player != null ? player.getName() : "--unknown--"));
		if (from != null) {
			sb.append(String.format(" %s,%5.0f,%3.0f,%5.0f", from.getWorld().getName(), from.getX(), from.getY(), from.getZ()));
		} else {
			sb.append(" (none)");
		}
		sb.append(" ->");
		if (to != null) {
			sb.append(String.format(" %s,%5.0f,%3.0f,%5.0f", to.getWorld().getName(), to.getX(), to.getY(), to.getZ()));
		} else {
			sb.append(" (none)");
		}
		plugin().log(sb.toString());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	private void monitorCombatLow(EntityDamageByEntityEvent event) {
		StringBuffer sb = new StringBuffer("[LO] ");
		logCombat(event, sb);
	}

	private void logCombat(EntityDamageByEntityEvent event, StringBuffer sb) {
		sb.append(event.isCancelled() ? "C " : "A ");
		sb.append(event.getCause().name());
		sb.append(String.format(", %5.2f->%5.2f", event.getDamage(), event.getFinalDamage()));
		sb.append(String.format(", %16s v %16s",
			event.getDamager() != null ? event.getDamager().getName() : "--unknown--",
			event.getEntity() != null ? event.getEntity().getName() : "--unknown--" ));
		for (EntityDamageEvent.DamageModifier mod : EntityDamageEvent.DamageModifier.values()) {
			sb.append(", ").append(mod.name());
			try {
				sb.append(String.format(" %5.2f->%5.2f", event.getOriginalDamage(mod), event.getDamage(mod)));
			} catch (Exception e) {
				sb.append(" --e--/--e--");
			}
		}
		plugin().log(sb.toString());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	private void monitorCombatHigh(EntityDamageByEntityEvent event) {
		StringBuffer sb = new StringBuffer("[HI]: ");
		logCombat(event, sb);
	}
}

