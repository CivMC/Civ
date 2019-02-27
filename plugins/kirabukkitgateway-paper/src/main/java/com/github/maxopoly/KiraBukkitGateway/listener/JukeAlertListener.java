package com.github.maxopoly.KiraBukkitGateway.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import com.untamedears.JukeAlert.events.PlayerHitSnitchEvent;

public class JukeAlertListener implements Listener {

	@EventHandler
	public void chat(PlayerHitSnitchEvent e) {
		KiraBukkitGatewayPlugin.getInstance().getRabbit().sendSnitchHit(e.getPlayer(),
				e.getSnitch().getLoc(), e.getSnitch().getName(), e.getSnitch().getGroup().getName());
	}

}
