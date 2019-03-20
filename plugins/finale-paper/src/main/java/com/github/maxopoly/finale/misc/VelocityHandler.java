package com.github.maxopoly.finale.misc;

import java.util.Map;
import java.util.Set;

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
	private Map<EntityType, Double> horizontalMultiplier;
	private Map<EntityType, Double> verticalMultiplier;
	private Map<EntityType, Float> pitchOffsets;
	private Map<EntityType, Float> powers;
	
	private Map<EntityType, Double> velocityMultiplier;
	
	public VelocityHandler(Set<EntityType> revertedTypes, Map<EntityType, Double> velocityMultiplier, Map<EntityType, Double> horizontalMultiplier, Map<EntityType, Double> verticalMultiplier,
			Map<EntityType, Float> pitchOffsets, Map<EntityType, Float> powers) {
		this.typesToRevert = revertedTypes;
		this.velocityMultiplier = velocityMultiplier;
		this.horizontalMultiplier = horizontalMultiplier;
		this.verticalMultiplier = verticalMultiplier;
		this.pitchOffsets = pitchOffsets;
		this.powers = powers;
	}

	public void modifyVelocity(Projectile projectile, Player shooter) {
		if (typesToRevert.contains(projectile.getType()) && powers.containsKey(projectile.getType())) {
			float power = powers.get(projectile.getType());
			float pitchOffset = pitchOffsets.containsKey(projectile.getType()) ? pitchOffsets.get(projectile.getType()) : 1;
			double horizontal = horizontalMultiplier.containsKey(projectile.getType()) ? horizontalMultiplier.get(projectile.getType()) : 1;
			double vertical = verticalMultiplier.containsKey(projectile.getType()) ? verticalMultiplier.get(projectile.getType()) : 1;
			shoot(projectile, shooter, power, pitchOffset, horizontal, vertical);
		}
		Double multiplier = velocityMultiplier.get(projectile.getType());
		if (multiplier == null) {
			return;
		}
		projectile.setVelocity(projectile.getVelocity().multiply(multiplier));
	}
	
	public void shoot(Projectile projectile, Player pShooter, float power, float pitchOffset, double horizontal, double vertical) {
		EntityPlayer shooter = ((CraftPlayer) pShooter).getHandle();
		EntityProjectile entityProj = ((CraftProjectile) projectile).getHandle();
		
		entityProj.setSize(0.25F, 0.25F);
		entityProj.setPositionRotation(shooter.locX, shooter.locY + shooter.getHeadHeight(), shooter.locZ, shooter.yaw, shooter.pitch);
		entityProj.locX -= (double)(MathHelper.cos(entityProj.yaw / 180.0F * (float)Math.PI) * 0.16F);
		entityProj.locY -= 0.10000000149011612D;
		entityProj.locZ -= (double)(MathHelper.sin(entityProj.yaw / 180.0F * (float)Math.PI) * 0.16F);
		entityProj.setPosition(entityProj.locX, entityProj.locY, entityProj.locZ);
    	
    	entityProj.motX = (double)(-MathHelper.sin(entityProj.yaw / 180.0F * (float)Math.PI) * MathHelper.cos(entityProj.pitch / 180.0F * (float)Math.PI) * horizontal);
    	entityProj.motZ = (double)(MathHelper.cos(entityProj.yaw / 180.0F * (float)Math.PI) * MathHelper.cos(entityProj.pitch / 180.0F * (float)Math.PI) * horizontal);
    	entityProj.motY = (double)(-MathHelper.sin((entityProj.pitch + pitchOffset) / 180.0F * (float)Math.PI) * vertical);
    	
        entityProj.shoot(entityProj.motX, entityProj.motY, entityProj.motZ, power, 1.0F);
	}

}
