package com.github.igotyou.FactoryMod.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

/**
 * This will only be registered as a listener if we want to disable portals
 *
 */
public class NetherPortalListener implements Listener {
	@EventHandler(priority = EventPriority.NORMAL)
	public void handlePortalTelportEvent(PlayerPortalEvent e) {
		// Disable normal nether portal teleportation
		if (e.getCause() == TeleportCause.NETHER_PORTAL) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void playerTeleportEvent(PlayerTeleportEvent e) {
		// Disable normal nether portal teleportation
		if (e.getCause() == TeleportCause.NETHER_PORTAL) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void entityTeleportEvent(EntityPortalEvent event) {
		event.setCancelled(true);
	}

}
