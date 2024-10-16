package com.github.maxopoly.finale.combat;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.combat.event.CritHitEvent;
import com.github.maxopoly.finale.combat.knockback.KnockbackStrategy;
import com.github.maxopoly.finale.misc.knockback.KnockbackConfig;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftVector;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

public class CombatUtil {
	
	 private static void sendSoundEffect(net.minecraft.world.entity.player.Player fromEntity, double x, double y, double z, SoundEvent soundEffect, SoundSource soundCategory, float volume, float pitch) {
        fromEntity.playSound(soundEffect, volume, pitch); // This will not send the effect to the entity himself
        if (fromEntity instanceof ServerPlayer) {
            ((ServerPlayer) fromEntity).connection.send(new ClientboundSoundPacket(soundEffect, soundCategory, x, y, z, volume, pitch));
        }
    }
	 
	public static void attack(Player attacker, Entity victim) {
		attack(((CraftPlayer) attacker).getHandle(), victim);
	}
	
	//see EntityHuman#attack(Entity) to update this
	public static void attack(net.minecraft.world.entity.player.Player attacker, Entity victim) {
		CombatConfig config = Finale.getPlugin().getManager().getCombatConfig();
		SprintHandler sprintHandler = Finale.getPlugin().getManager().getSprintHandler();
        if (victim.isAttackable() && !victim.skipAttackInteraction(attacker)) {
			float damage = (float) attacker.getAttribute(Attributes.ATTACK_DAMAGE).getValue();
			InteractionHand swingingArm = attacker.swingingArm != null ? attacker.swingingArm : InteractionHand.MAIN_HAND;
			float f1 = (victim instanceof LivingEntity) ?
					EnchantmentHelper.getDamageBonus(attacker.getItemInHand(swingingArm), ((LivingEntity) victim).getMobType()) :
					EnchantmentHelper.getDamageBonus(attacker.getItemInHand(swingingArm), MobType.UNDEFINED);

			float f2 = 0;
			boolean shouldKnockback = true;
			if (config.isAttackCooldownEnabled()) {
				f2 = attacker.getAttackStrengthScale(0.5F);
				shouldKnockback = f2 > 0.9f;
				damage *= 0.2F + f2 * f2 * 0.8F;
				f1 *= f2;
			}
			Level world = attacker.getLevel();
			if (damage > 0.0F || f1 > 0.0F) {
				boolean dealtExtraKnockback = false;
				byte baseKnockbackLevel = 1;
				int knockbackLevel = baseKnockbackLevel + EnchantmentHelper.getKnockbackBonus(attacker);

				if (sprintHandler.isSprinting(attacker) && shouldKnockback) {
					if (config.getCombatSounds().isKnockbackEnabled()) {
						sendSoundEffect(attacker, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, attacker.getSoundSource(), 1.0F, 1.0F); // Paper - send while respecting visibility
					}
					if (!config.isKnockbackSwordsEnabled()) {
						++knockbackLevel;
					}
					dealtExtraKnockback = true;
				}

				boolean shouldCrit = shouldKnockback && attacker.fallDistance > 0.0F && !attacker.isOnGround() && !attacker.onClimbable() && !attacker.isInWater()
						&& !attacker.hasEffect(MobEffects.BLINDNESS) && !attacker.isPassenger() && victim instanceof LivingEntity;
				shouldCrit = shouldCrit && !sprintHandler.isSprinting(attacker);
				if (shouldCrit) {
					double critMultiplier = 1.5d;
					if ((attacker.getBukkitEntity() instanceof Player) && (victim.getBukkitEntity() instanceof org.bukkit.entity.LivingEntity)) {
						CritHitEvent critHitEvent = new CritHitEvent(((Player) attacker.getBukkitEntity()), ((org.bukkit.entity.LivingEntity) victim.getBukkitEntity()), critMultiplier);
						org.bukkit.Bukkit.getPluginManager().callEvent(critHitEvent);

						critMultiplier = critHitEvent.getCritMultiplier();
					}

					damage *= critMultiplier;
				}
				damage += f1;
				boolean shouldSweep = false;
				double d0 = attacker.walkDist - attacker.walkDistO;

				if (shouldKnockback && !shouldCrit && !dealtExtraKnockback && attacker.isOnGround() && d0 < (double) attacker.getSpeed()) {
					ItemStack itemstack = attacker.getItemInHand(InteractionHand.MAIN_HAND);
					if (itemstack.getItem() instanceof SwordItem) {
						shouldSweep = true;
					}
				}

				float victimHealth = 0.0F;
				boolean onFire = false;
				int fireAspectEnchantmentLevel = EnchantmentHelper.getFireAspect(attacker);
				if (victim instanceof LivingEntity) {
					victimHealth = ((LivingEntity) victim).getHealth();
					if (fireAspectEnchantmentLevel > 0 && !victim.isOnFire()) {
						EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(attacker.getBukkitEntity(), victim.getBukkitEntity(), 1);
						org.bukkit.Bukkit.getPluginManager().callEvent(combustEvent);

						if (!combustEvent.isCancelled()) {
							onFire = true;
							victim.setSecondsOnFire(combustEvent.getDuration(), false);
						}
					}
				}

				Vec3 victimMot = victim.getDeltaMovement();
				boolean damagedVictim = victim.hurt(DamageSource.playerAttack(attacker), damage);
				if (damagedVictim) {
					if (victim instanceof LivingEntity) {
						KnockbackStrategy knockbackStrategy = config.getKnockbackStrategy();
						LivingEntity livingVictim = (LivingEntity) victim;

						knockbackStrategy.handleKnockback(attacker, livingVictim, knockbackLevel);
					}

					//((CraftPlayer)attacker.getBukkitEntity()).sendMessage("motX: " + victim.motX + ", motY: " + victim.motY + ", motZ: " + victim.motZ + ", onGround: " + victim.onGround);

					if (shouldSweep && config.isSweepEnabled()) {
						float f4 = 1.0F + EnchantmentHelper.getSweepingDamageRatio(attacker) * damage;
						List<LivingEntity> list = attacker.getLevel().getEntitiesOfClass(LivingEntity.class, victim.getBoundingBox().inflate(1.0D, 0.25D, 1.0D));
						Iterator<LivingEntity> iterator = list.iterator();

						while (iterator.hasNext()) {
							LivingEntity entityliving = iterator.next();

							if (entityliving != attacker && entityliving != victim && !attacker.skipAttackInteraction(entityliving) && (!(entityliving instanceof ArmorStand) || !((ArmorStand) entityliving).isMarker()) && attacker.distanceToSqr(entityliving) < 9.0D) {
								// CraftBukkit start - Only apply knockback if the damage hits
								if (entityliving.hurt(DamageSource.playerAttack(attacker).sweep(), f4)) {
									entityliving.knockback(0.4F, (double) Mth.sin(attacker.getBukkitYaw() * 0.017453292F), (double) (-Mth.cos(attacker.getBukkitYaw() * 0.017453292F)));
								}
								// CraftBukkit end
							}
						}

						attacker.getLevel().playSound(attacker, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, attacker.getSoundSource(), 1.0F, 1.0F); // Paper - send while respecting visibility
						attacker.sweepAttack();
					}

					if (victim instanceof ServerPlayer && victim.hurtMarked) {
						boolean cancelled = false;
						Player player = (Player) victim.getBukkitEntity();
						Vector velocity = CraftVector.toBukkit(victimMot);

						PlayerVelocityEvent event = new PlayerVelocityEvent(player, velocity.clone());
						world.getCraftServer().getPluginManager().callEvent(event);

						if (event.isCancelled()) {
							cancelled = true;
						} else if (!velocity.equals(event.getVelocity())) {
							player.setVelocity(event.getVelocity());
						}

						if (!cancelled) {
							((ServerPlayer) victim).connection.send(new ClientboundSetEntityMotionPacket(victim));
							victim.hurtMarked = false;
							victim.setDeltaMovement(victimMot);
						}
						// CraftBukkit end
					}

					if (shouldCrit) {
						if (config.getCombatSounds().isCritEnabled()) {
							sendSoundEffect(attacker, attacker.getX(), attacker.getY(), attacker.getZ(),
									SoundEvents.PLAYER_ATTACK_CRIT, attacker.getSoundSource(), 1.0F, 1.0F);
						}
						attacker.crit(victim);
					}

					if (shouldCrit || shouldSweep) {
						if (shouldKnockback && config.getCombatSounds().isStrongEnabled()) {
							attacker.getLevel().playSound(attacker, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, attacker.getSoundSource(), 1.0F, 1.0F); // Paper - send while respecting visibility
						} else if (config.getCombatSounds().isWeakEnabled()) {
							attacker.getLevel().playSound(attacker, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, attacker.getSoundSource(), 1.0F, 1.0F); // Paper - send while respecting visibility
						}
					}

					if (f1 > 0.0F) {
						attacker.magicCrit(victim);
					}

					attacker.setLastHurtMob(victim);
					if (victim instanceof LivingEntity) {
						EnchantmentHelper.doPostHurtEffects((LivingEntity) victim, attacker);
					}

					EnchantmentHelper.doPostDamageEffects(attacker, victim);

					InteractionHand hand = attacker.swingingArm != null ? attacker.swingingArm : InteractionHand.MAIN_HAND;
					ItemStack itemstack1 = attacker.getItemInHand(hand);
					Object object = victim;

					if (victim instanceof EnderDragonPart) {
						object = ((EnderDragonPart) victim).parentMob;
					}

					if (!attacker.getLevel().isClientSide() && !itemstack1.isEmpty() && object instanceof LivingEntity) {
						itemstack1.hurtEnemy((LivingEntity) object, attacker);
						if (itemstack1.isEmpty()) {
							attacker.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
						}
					}

					if (victim instanceof LivingEntity) {
						float f5 = victimHealth - ((LivingEntity) victim).getHealth();

						attacker.awardStat(Stats.DAMAGE_DEALT, Math.round(f5 * 10.0F));
						if (fireAspectEnchantmentLevel > 0) {
							// CraftBukkit start - Call a combust event when somebody hits with a fire enchanted item
							EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(attacker.getBukkitEntity(), victim.getBukkitEntity(), fireAspectEnchantmentLevel * 4);
							org.bukkit.Bukkit.getPluginManager().callEvent(combustEvent);

							if (!combustEvent.isCancelled()) {
								victim.setSecondsOnFire(combustEvent.getDuration());
							}
							// CraftBukkit end
						}

						if (world instanceof ServerLevel && f5 > 2.0F) {
							int k = (int) ((double) f5 * 0.5D);

							((ServerLevel) world).sendParticles(ParticleTypes.DAMAGE_INDICATOR, victim.getX(),
									victim.getY() + (double) (victim.getEyeHeight() * 0.5F), victim.getZ(), k, 0.1D, 0.0D, 0.1D,
									0.2D);
						}
					}

					attacker.causeFoodExhaustion(world.spigotConfig.combatExhaustion); // Spigot - Change to use configurable value
				} else {
					//sendSoundEffect(attacker, attacker.locX, attacker.locY, attacker.locZ, SoundEffects.fx, attacker.bK(), 1.0F, 1.0F); // Paper - send while respecting visibility
					if (onFire) {
						victim.clearFire();
					}
					// CraftBukkit start - resync on cancelled event
					if (attacker instanceof ServerPlayer) {
						((ServerPlayer) attacker).getBukkitEntity().updateInventory();
					}
					// CraftBukkit end
				}
			}
        }
    }
}

