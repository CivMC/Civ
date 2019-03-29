package com.github.maxopoly.KiraBukkitGateway.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import com.untamedears.JukeAlert.events.PlayerHitSnitchEvent;
import com.untamedears.JukeAlert.events.PlayerLoginSnitchEvent;
import com.untamedears.JukeAlert.events.PlayerLogoutSnitchEvent;
import com.untamedears.JukeAlert.model.Snitch;

public class JukeAlertListener implements Listener {

	@EventHandler
	public void enter(PlayerHitSnitchEvent e) {
		KiraBukkitGatewayPlugin.getInstance().getRabbit().sendSnitchHit(e.getPlayer(), e.getSnitch().getLoc(),
				e.getSnitch().getName(), e.getSnitch().getGroup().getName(), SnitchHitType.ENTER,
				getSnitchType(e.getSnitch()));
	}

	@EventHandler
	public void login(PlayerLoginSnitchEvent e) {
		KiraBukkitGatewayPlugin.getInstance().getRabbit().sendSnitchHit(e.getPlayer(), e.getSnitch().getLoc(),
				e.getSnitch().getName(), e.getSnitch().getGroup().getName(), SnitchHitType.LOGIN,
				getSnitchType(e.getSnitch()));
	}

	@EventHandler
	public void login(PlayerLogoutSnitchEvent e) {
		KiraBukkitGatewayPlugin.getInstance().getRabbit().sendSnitchHit(e.getPlayer(), e.getSnitch().getLoc(),
				e.getSnitch().getName(), e.getSnitch().getGroup().getName(), SnitchHitType.LOGOUT,
				getSnitchType(e.getSnitch()));
	}

	private SnitchType getSnitchType(Snitch snitch) {
		if (snitch.shouldLog()) {
			return SnitchType.LOGGING;
		}
		return SnitchType.ENTRY;
	}

}
