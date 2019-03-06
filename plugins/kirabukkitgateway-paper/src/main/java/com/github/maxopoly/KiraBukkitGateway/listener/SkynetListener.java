package com.github.maxopoly.KiraBukkitGateway.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;

public class SkynetListener implements Listener {

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void join(PlayerJoinEvent e) {
		KiraBukkitGatewayPlugin.getInstance().getRabbit().playerLoginOut(e.getPlayer().getName(), "login");
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void leave(PlayerQuitEvent e) {
		KiraBukkitGatewayPlugin.getInstance().getRabbit().playerLoginOut(e.getPlayer().getName(), "logout");
	}



}
