package com.github.civcraft.donum.listeners.storage;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.github.civcraft.donum.Donum;

import vg.civcraft.mc.bettershards.events.PlayerChangeServerEvent;



public class BetterShardsListener implements Listener {
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void playerLeave(PlayerChangeServerEvent e) {
		Player p = Bukkit.getPlayer(e.getPlayerUUID());
		Donum.getManager().savePlayerData(e.getPlayerUUID(), p.getInventory(), false);
	}
}
