package com.github.maxopoly.KiraBukkitGateway.listener;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SkynetListener implements Listener {

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void join(PlayerJoinEvent e) {
		if (!e.getPlayer().hasPlayedBefore()) {
			KiraBukkitGatewayPlugin.getInstance().getRabbit().playerLoginFirstTime(e.getPlayer().getName());
		}
		KiraBukkitGatewayPlugin.getInstance().getRabbit().playerLoginOut(e.getPlayer().getName(), "LOGIN");
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void leave(PlayerQuitEvent e) {
		KiraBukkitGatewayPlugin.getInstance().getRabbit().playerLoginOut(e.getPlayer().getName(), "LOGOUT");
	}

}
