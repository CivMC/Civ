package com.github.maxopoly.KiraBukkitGateway.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import com.untamedears.jukealert.events.PlayerHitSnitchEvent;
import com.untamedears.jukealert.events.PlayerLoginSnitchEvent;
import com.untamedears.jukealert.events.PlayerLogoutSnitchEvent;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;

public class JukeAlertListener implements Listener {

	@EventHandler
	public void enter(PlayerHitSnitchEvent e) {
		if (immune(e.getSnitch(), e.getPlayer())) {
			return;
		}
		KiraBukkitGatewayPlugin.getInstance().getRabbit().sendSnitchHit(e.getPlayer(), e.getSnitch().getLocation(),
				e.getSnitch().getName(), e.getSnitch().getGroup().getName(), SnitchHitType.ENTER,
				e.getSnitch().getType().getName());
	}

	@EventHandler
	public void login(PlayerLoginSnitchEvent e) {
		if (immune(e.getSnitch(), e.getPlayer())) {
			return;
		}
		KiraBukkitGatewayPlugin.getInstance().getRabbit().sendSnitchHit(e.getPlayer(), e.getSnitch().getLocation(),
				e.getSnitch().getName(), e.getSnitch().getGroup().getName(), SnitchHitType.LOGIN,
				e.getSnitch().getType().getName());
		
	}

	@EventHandler
	public void login(PlayerLogoutSnitchEvent e) {
		if (immune(e.getSnitch(), e.getPlayer())) {
			return;
		}
		KiraBukkitGatewayPlugin.getInstance().getRabbit().sendSnitchHit(e.getPlayer(), e.getSnitch().getLocation(),
				e.getSnitch().getName(), e.getSnitch().getGroup().getName(), SnitchHitType.LOGOUT,
				e.getSnitch().getType().getName());
	}
	
	private static boolean immune(Snitch snitch, Player p) {
		return snitch.hasPermission(p, JukeAlertPermissionHandler.getSnitchImmune());
	}

}
