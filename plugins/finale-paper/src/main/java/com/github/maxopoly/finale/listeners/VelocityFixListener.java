package com.github.maxopoly.finale.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import com.github.maxopoly.finale.misc.VelocityHandler;

public class VelocityFixListener implements Listener {
	
	private VelocityHandler velocityManager;
	
	public VelocityFixListener(VelocityHandler velocityManager) {
		this.velocityManager = velocityManager;
	}
	
	@EventHandler
	public void projectileLaunch(ProjectileLaunchEvent e) {
		if(!(e.getEntity().getShooter() instanceof Player)) {
			return;
		}
		velocityManager.modifyVelocity(e.getEntity(), (Player) e.getEntity().getShooter());
	}

}
