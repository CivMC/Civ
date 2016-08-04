package com.github.civcraft.donum.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.github.civcraft.donum.Donum;

public class LogInOutListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerJoin(PlayerJoinEvent e) {
		Donum.getManager().loadPlayerData(e.getPlayer().getUniqueId(), e.getPlayer().getInventory());
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent e) {
		Donum.getManager().savePlayerData(e.getPlayer().getUniqueId(), e.getPlayer().getInventory());
	}
}
