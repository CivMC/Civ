package com.github.maxopoly.finale.combat.knockback;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.combat.CombatConfig;
import com.github.maxopoly.finale.combat.SprintHandler;
import com.github.maxopoly.finale.misc.knockback.KnockbackConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.bukkit.util.Vector;

public class StandardKnockback implements KnockbackStrategy {

	@Override
	public void handleKnockback(Player attacker, Entity entity, int knockbackLevel) {
		CombatConfig config = Finale.getPlugin().getManager().getCombatConfig();
		SprintHandler sprintHandler = Finale.getPlugin().getManager().getSprintHandler();

		if (entity instanceof LivingEntity) {
			LivingEntity victim = (LivingEntity) entity;
			if (sprintHandler.isSprinting(attacker)) {
				knockbackLevel++;
			}
			double strength = knockbackLevel * config.getKnockbackLevelMultiplier() * 0.5;
			double x = Mth.sin(attacker.getYRot() * 0.017453292F);
			double z = (-Mth.cos(attacker.getYRot() * 0.017453292F));
			strength *= 1.0D - victim.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
			if (strength > 0.0) {
				victim.hasImpulse = true;
				Vec3 vec3d = victim.getDeltaMovement();
				Vec3 vec3d1 = (new Vec3(x, 0.0D, z)).normalize().scale(strength);

				Vector victimMotion = config.getVictimMotion();

				double dx = (vec3d.x * victimMotion.getX()) - vec3d1.x;
				//double dy = victim.onGround ? Math.min(0.4D, (vec3d.y * victimMotion.getY()) + strength) : vec3d.y;
				double dy = (vec3d.y * victimMotion.getY());
				double dz = (vec3d.z * victimMotion.getZ()) - vec3d1.z;

				Vector start = new Vector(vec3d.x, vec3d.y, vec3d.z);
				Vector dv = new Vector(dx, dy, dz);

				boolean isSprinting = sprintHandler.isSprinting(attacker);
				KnockbackConfig knockbackConfig = isSprinting ? config.getSprintConfig() : config.getNormalConfig();

				if (victim.isInWater()) {
					dv = knockbackConfig.getWaterModifier().modifyKnockback(start, dv);
				} else {
					if (!victim.isOnGround()) {
						dv = knockbackConfig.getAirModifier().modifyKnockback(start, dv);
					} else {
						dv = knockbackConfig.getGroundModifier().modifyKnockback(start, dv);
					}
				}

				victim.setDeltaMovement(dv.getX(), dv.getY(), dv.getZ());

				Vec3 currentMovement = victim.getDeltaMovement();
				org.bukkit.util.Vector delta = new org.bukkit.util.Vector(currentMovement.x - vec3d.x, currentMovement.y - vec3d.y, currentMovement.z - vec3d.z);

				victim.setDeltaMovement(vec3d);
				if (attacker == null || new com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent((org.bukkit.entity.LivingEntity) victim.getBukkitEntity(), attacker.getBukkitEntity(), (float) strength, delta).callEvent()) {
					double dmx = vec3d.x + delta.getX();
					double dmy = vec3d.y + delta.getY();
					double dmz = vec3d.z + delta.getZ();
					Vector maxVictimMotion = config.getMaxVictimMotion();

					victim.setDeltaMovement(
							Math.min(dmx, maxVictimMotion.getX()),
							Math.min(dmy, maxVictimMotion.getY()),
							Math.min(dmz, maxVictimMotion.getZ())
					);
				}
			}
		} else {
			entity.push((-Mth.sin(attacker.getYRot() * 0.017453292F) * (float) knockbackLevel * 0.5F), 0.1D, (double) (Mth.cos(attacker.getYRot() * 0.017453292F) * (float) knockbackLevel * 0.5F));
		}

		Vector attackerMotion = config.getAttackerMotion();
		attacker.setDeltaMovement(attacker.getDeltaMovement().multiply(attackerMotion.getX(), attackerMotion.getY(), attackerMotion.getZ()));
		if (attacker.isInWater()) {
			if (config.isWaterSprintResetEnabled()) {
				//attacker.setSprinting(false);
				sprintHandler.stopSprinting(attacker);
			}
		} else {
			if (config.isSprintResetEnabled()) {
				//attacker.setSprinting(false); // looks like modern minecraft finally defeated w-tapping, but we work around it
				sprintHandler.stopSprinting(attacker);
			}
		}
	}

}
