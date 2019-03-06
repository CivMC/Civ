package com.github.maxopoly.finale.misc;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

public class VelocityHandler {
	
	private Set<EntityType> typesToRevert;
	private Map<EntityType, Double> velocityMultiplier;
	
	public VelocityHandler(Collection<EntityType> revertedTypes, Map<EntityType, Double> velocityMultiplier) {
		this.typesToRevert = new TreeSet<>();
		this.velocityMultiplier = velocityMultiplier;
	}
	
	public void modifyVelocity(Projectile projectile, Player shooter) {
		if (typesToRevert.contains(projectile.getType())) {
			projectile.setVelocity(projectile.getVelocity().subtract(shooter.getVelocity()));
		}
		Double multiplier = velocityMultiplier.get(projectile.getType());
		if (multiplier == null) {
			return;
		}
		projectile.setVelocity(projectile.getVelocity().multiply(multiplier));
	}

}
