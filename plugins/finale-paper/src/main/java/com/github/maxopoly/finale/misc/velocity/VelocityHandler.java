package com.github.maxopoly.finale.misc.velocity;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

public class VelocityHandler {

	private Map<EntityType, VelocityConfig> velocityConfigs;
	
	public VelocityHandler(Map<EntityType, VelocityConfig> velocityConfigs) {
		this.velocityConfigs = velocityConfigs;
	}

	public void modifyVelocity(Projectile projectile, Player shooter) {
		if (!velocityConfigs.containsKey(projectile.getType())) return;
		
		VelocityConfig velocityConfig = velocityConfigs.get(projectile.getType());
		Vector newVel = velocityConfig.modifyLaunchVelocity(projectile, shooter);
		projectile.setVelocity(newVel);
	}

}
