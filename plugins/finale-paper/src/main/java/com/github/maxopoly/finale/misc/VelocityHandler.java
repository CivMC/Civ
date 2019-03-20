package com.github.maxopoly.finale.misc;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftProjectile;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EntityPotion;
import net.minecraft.server.v1_13_R2.EntityProjectile;
import net.minecraft.server.v1_13_R2.MathHelper;

public class VelocityHandler {
	
	private Set<EntityType> typesToRevert;
	private Map<EntityType, Double> velocityMultiplier;
	private Map<EntityType, Float> powers;
	
	public VelocityHandler(Collection<EntityType> revertedTypes, Map<EntityType, Double> velocityMultiplier, Map<EntityType, Float> powers) {
		this.typesToRevert = new TreeSet<>();
		this.velocityMultiplier = velocityMultiplier;
		this.powers = powers;
	}
	
	public void modifyVelocity(Projectile projectile, Player shooter) {
		if (typesToRevert.contains(projectile.getType()) && powers.containsKey(projectile.getType())) {
			float power = powers.get(projectile.getType());
			shoot(projectile, shooter, power);
		}
		Double multiplier = velocityMultiplier.get(projectile.getType());
		if (multiplier == null) {
			return;
		}
		projectile.setVelocity(projectile.getVelocity().multiply(multiplier));
	}
	
	public void shoot(Projectile projectile, Player pShooter, float power) {
		EntityPlayer shooter = ((CraftPlayer) pShooter).getHandle();
		EntityProjectile entityProj = ((CraftProjectile) projectile).getHandle();
		
		entityProj.setSize(0.25F, 0.25F);
		entityProj.setPositionRotation(shooter.locX, shooter.locY + shooter.getHeadHeight(), shooter.locZ, shooter.yaw, shooter.pitch);
		entityProj.locX -= (double)(MathHelper.cos(entityProj.yaw / 180.0F * (float)Math.PI) * 0.16F);
		entityProj.locY -= 0.10000000149011612D;
		entityProj.locZ -= (double)(MathHelper.sin(entityProj.yaw / 180.0F * (float)Math.PI) * 0.16F);
		entityProj.setPosition(entityProj.locX, entityProj.locY, entityProj.locZ);
    	
		float f = -20;
    	float var3 = 0.4F;
    	entityProj.motX = (double)(-MathHelper.sin(entityProj.yaw / 180.0F * (float)Math.PI) * MathHelper.cos(entityProj.pitch / 180.0F * (float)Math.PI) * var3);
    	entityProj.motZ = (double)(MathHelper.cos(entityProj.yaw / 180.0F * (float)Math.PI) * MathHelper.cos(entityProj.pitch / 180.0F * (float)Math.PI) * var3);
    	entityProj.motY = (double)(-MathHelper.sin((entityProj.pitch + f) / 180.0F * (float)Math.PI) * var3);
    	
        entityProj.shoot(entityProj.motX, entityProj.motY, entityProj.motZ, power, 1.0F);
	}

}
