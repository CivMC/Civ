package com.github.maxopoly.finale.combat;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.combat.event.CritHitEvent;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.particles.Particles;
import net.minecraft.network.protocol.game.PacketPlayOutEntityVelocity;
import net.minecraft.network.protocol.game.PacketPlayOutNamedSoundEffect;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumMonsterType;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.boss.EntityComplexPart;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemSword;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftVector;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class CombatUtil {
	
	 private static void sendSoundEffect(EntityHuman fromEntity, double x, double y, double z, SoundEffect soundEffect, SoundCategory soundCategory, float volume, float pitch) {
        fromEntity.getWorld().playSound(fromEntity, x, y, z, soundEffect, soundCategory, volume, pitch); // This will not send the effect to the entity himself
        if (fromEntity instanceof EntityPlayer) {
            ((EntityPlayer) fromEntity).b.sendPacket(new PacketPlayOutNamedSoundEffect(soundEffect, soundCategory, x, y, z, volume, pitch));
        }
    }
	 
	public static void attack(Player attacker, LivingEntity victim) {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				attack(((CraftPlayer) attacker).getHandle(), ((CraftLivingEntity) victim).getHandle());
			}
			
		}.runTask(Finale.getPlugin());
	}
	
	//see EntityHuman#attack(Entity) to update this
	public static void attack(EntityHuman attacker, Entity victim) {
		CombatConfig config = Finale.getPlugin().getManager().getCombatConfig();
        if (victim.bL() && !victim.r(attacker)) {
			float damage = (float) attacker.getAttributeInstance(GenericAttributes.f).getValue();
			float f1 = (victim instanceof EntityLiving) ?
					EnchantmentManager.a(attacker.getItemInMainHand(), ((EntityLiving) victim).getMonsterType()) :
					EnchantmentManager.a(attacker.getItemInMainHand(), EnumMonsterType.a);

			float f2 = 0;
			boolean shouldKnockback = true;
			if (config.isAttackCooldownEnabled()) {
				f2 = attacker.getAttackCooldown(0.5F);
				shouldKnockback = f2 > 0.9f;
				damage *= 0.2F + f2 * f2 * 0.8F;
				f1 *= f2;
			}
			World world = attacker.getWorld();
			if (damage > 0.0F || f1 > 0.0F) {
				boolean dealtExtraKnockback = false;
				byte baseKnockbackLevel = 0;
				int knockbackLevel = baseKnockbackLevel + EnchantmentManager.b((EntityLiving) attacker);

				if (attacker.isSprinting() && shouldKnockback) {
					if (config.getCombatSounds().isKnockbackEnabled()) {
						sendSoundEffect(attacker, attacker.locX(), attacker.locY(), attacker.locZ(), SoundEffects.ok, attacker.getSoundCategory(), 1.0F, 1.0F); // Paper - send while respecting visibility
					}
					if (!config.isKnockbackSwordsEnabled()) {
						++knockbackLevel;
					}
					dealtExtraKnockback = true;
				}

				boolean shouldCrit = shouldKnockback && attacker.K > 0.0F && !attacker.isOnGround() && !attacker.isClimbing() && !attacker.isInWater()
						&& !attacker.hasEffect(MobEffects.o) && !attacker.isPassenger() && victim instanceof EntityLiving;
				shouldCrit = shouldCrit && !attacker.isSprinting();
				if (shouldCrit) {
					double critMultiplier = 1.5d;
					if ((attacker.getBukkitEntity() instanceof Player) && (victim.getBukkitEntity() instanceof LivingEntity)) {
						CritHitEvent critHitEvent = new CritHitEvent(((Player) attacker.getBukkitEntity()), ((LivingEntity) victim.getBukkitEntity()), critMultiplier);
						org.bukkit.Bukkit.getPluginManager().callEvent(critHitEvent);

						critMultiplier = critHitEvent.getCritMultiplier();
					}

					damage *= critMultiplier;
				}
				damage += f1;
				boolean shouldSweep = false;
				double d0 = attacker.H - attacker.G;

				if (shouldKnockback && !shouldCrit && !dealtExtraKnockback && attacker.isOnGround() && d0 < (double) attacker.ev()) {
					ItemStack itemstack = attacker.b(EnumHand.a);
					if (itemstack.getItem() instanceof ItemSword) {
						shouldSweep = true;
					}
				}

				float victimHealth = 0.0F;
				boolean onFire = false;
				int fireAspectEnchantmentLevel = EnchantmentManager.getFireAspectEnchantmentLevel(attacker);
				if (victim instanceof EntityLiving) {
					victimHealth = ((EntityLiving) victim).getHealth();
					if (fireAspectEnchantmentLevel > 0 && !victim.isBurning()) {
						EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(attacker.getBukkitEntity(), victim.getBukkitEntity(), 1);
						org.bukkit.Bukkit.getPluginManager().callEvent(combustEvent);

						if (!combustEvent.isCancelled()) {
							onFire = true;
							victim.setOnFire(combustEvent.getDuration(), false);
						}
					}
				}

				Vec3D victimMot = victim.getMot();
				boolean damagedVictim = victim.damageEntity(DamageSource.playerAttack(attacker), damage);
				if (damagedVictim) {
					if (victim instanceof EntityLiving) {
						EntityLiving livingVictim = (EntityLiving) victim;
						double kbResistance = livingVictim.b(GenericAttributes.c);
						double knockbackModifier = (1.0 - kbResistance);
						double knockbackLevelModifier = 1 + (knockbackLevel * config.getKnockbackLevelMultiplier());

						if (knockbackModifier > 0) {
							double dx = -MathHelper.sin(attacker.getBukkitYaw() * 0.01745329251f) * 0.3f;
							double dy = 0.35;
							double dz = MathHelper.cos(attacker.getBukkitYaw() * 0.01745329251f) * 0.3f;

							Vector knockbackMultiplier = config.getKnockbackMultiplier();
							Vector waterKnockbackMultiplier = config.getWaterKnockbackMultiplier();
							Vector airKnockbackMultiplier = config.getAirKnockbackMultiplier();

							dx *= knockbackMultiplier.getX();
							dy *= knockbackMultiplier.getY();
							dz *= knockbackMultiplier.getZ();

							if (dealtExtraKnockback) {
								Vector sprintMultiplier = config.getSprintMultiplier();
								dx *= sprintMultiplier.getX();
								dy *= sprintMultiplier.getY();
								dz *= sprintMultiplier.getZ();
							}

							if (victim.isInWater()) {
								dx *= waterKnockbackMultiplier.getX();
								dy *= waterKnockbackMultiplier.getY();
								dz *= waterKnockbackMultiplier.getZ();
							} else if (!victim.isOnGround()) {
								dx *= airKnockbackMultiplier.getX();
								dy *= airKnockbackMultiplier.getY();
								dz *= airKnockbackMultiplier.getZ();
							}

							if (config.isKnockbackSwordsEnabled() && knockbackLevel > 1) {
								dx *= knockbackLevelModifier;
								dz *= knockbackLevelModifier;
							}

							dx *= knockbackModifier;
							dy *= knockbackModifier;
							dz *= knockbackModifier;

							victim.af = true;

							Vector victimMotFactor = config.getVictimMotion();
							Vector maxVictimMot = config.getMaxVictimMotion();
							double motX = Math.min((victimMot.getX() * victimMotFactor.getX()) + dx, maxVictimMot.getX());
							double motY = Math.min((victimMot.getY() * victimMotFactor.getY()) + dy, maxVictimMot.getY());
							double motZ = Math.min((victimMot.getZ() * victimMotFactor.getZ()) + dz, maxVictimMot.getZ());

							Vec3D newVictimMot = new Vec3D(motX, motY, motZ);
							victim.setMot(newVictimMot);
							victimMot = newVictimMot;
						} else {
							victim.i(
									(-MathHelper.sin(attacker.getBukkitYaw() * 0.017453292f) * knockbackLevel * 0.5f),
									0.1,
									(MathHelper.cos(attacker.getBukkitYaw() * 0.017453292f) * knockbackLevel * 0.5f)
							);
						}
					}
					Vector attackerMotion = config.getAttackerMotion();
					attacker.setMot(attacker.getMot().d(attackerMotion.getX(), attackerMotion.getY(), attackerMotion.getZ()));
					if (attacker.isInWater()) {
						attacker.setSprinting(!config.isWaterSprintResetEnabled());
					} else {
						attacker.setSprinting(!config.isSprintResetEnabled());
					}

					//((CraftPlayer)attacker.getBukkitEntity()).sendMessage("motX: " + victim.motX + ", motY: " + victim.motY + ", motZ: " + victim.motZ + ", onGround: " + victim.onGround);

					if (shouldSweep && config.isSweepEnabled()) {
						float f4 = 1.0F + EnchantmentManager.a((EntityLiving) attacker) * damage;
						List<EntityLiving> list = attacker.getWorld().a(EntityLiving.class, victim.getBoundingBox().grow(1.0D, 0.25D, 1.0D));
						Iterator<EntityLiving> iterator = list.iterator();

						while (iterator.hasNext()) {
							EntityLiving entityliving = (EntityLiving) iterator.next();

							if (entityliving != attacker && entityliving != victim && !attacker.r(entityliving) && (!(entityliving instanceof EntityArmorStand) || !((EntityArmorStand) entityliving).isMarker()) && attacker.f(entityliving) < 9.0D) {
								// CraftBukkit start - Only apply knockback if the damage hits
								if (entityliving.damageEntity(DamageSource.playerAttack(attacker).sweep(), f4)) {
									entityliving.a(0.4F, (double) MathHelper.sin(attacker.getBukkitYaw() * 0.017453292F), (double) (-MathHelper.cos(attacker.getBukkitYaw() * 0.017453292F)));
								}
								// CraftBukkit end
							}
						}

						attacker.getWorld().playSound(attacker, attacker.locX(), attacker.locY(), attacker.locZ(), SoundEffects.on, attacker.getSoundCategory(), 1.0F, 1.0F); // Paper - send while respecting visibility
						attacker.ff();
					}

					if (victim instanceof EntityPlayer && victim.C) {
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
							((EntityPlayer) victim).b.sendPacket(new PacketPlayOutEntityVelocity(victim));
							victim.C = false;
							victim.setMot(victimMot);
						}
						// CraftBukkit end
					}

					if (shouldCrit) {
						if (config.getCombatSounds().isCritEnabled()) {
							sendSoundEffect(attacker, attacker.locX(), attacker.locY(), attacker.locZ(),
									SoundEffects.oj, attacker.getSoundCategory(), 1.0F, 1.0F);
						}
						attacker.a(victim);
					}

					if (shouldCrit || shouldSweep) {
						if (shouldKnockback && config.getCombatSounds().isStrongEnabled()) {
							attacker.getWorld().playSound(attacker, attacker.locX(), attacker.locY(), attacker.locZ(), SoundEffects.om, attacker.getSoundCategory(), 1.0F, 1.0F); // Paper - send while respecting visibility
						} else if (config.getCombatSounds().isWeakEnabled()) {
							attacker.getWorld().playSound(attacker, attacker.locX(), attacker.locY(), attacker.locZ(), SoundEffects.oo, attacker.getSoundCategory(), 1.0F, 1.0F); // Paper - send while respecting visibility
						}
					}

					if (f1 > 0.0F) {
						attacker.b(victim);
					}

					attacker.x(victim);
					if (victim instanceof EntityLiving) {
						EnchantmentManager.a((EntityLiving) victim, (Entity) attacker);
					}

					EnchantmentManager.b((EntityLiving) attacker, victim);
					ItemStack itemstack1 = attacker.getItemInMainHand();
					Object object = victim;

					if (victim instanceof EntityComplexPart) {
						object = ((EntityComplexPart) victim).b;
					}

					if (!attacker.getWorld().isClientSide() && !itemstack1.isEmpty() && object instanceof EntityLiving) {
						itemstack1.a((EntityLiving) object, attacker);
						if (itemstack1.isEmpty()) {
							attacker.a(EnumHand.a, ItemStack.b);
						}
					}

					if (victim instanceof EntityLiving) {
						float f5 = victimHealth - ((EntityLiving) victim).getHealth();

						attacker.a(StatisticList.G, Math.round(f5 * 10.0F));
						if (fireAspectEnchantmentLevel > 0) {
							// CraftBukkit start - Call a combust event when somebody hits with a fire enchanted item
							EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(attacker.getBukkitEntity(), victim.getBukkitEntity(), fireAspectEnchantmentLevel * 4);
							org.bukkit.Bukkit.getPluginManager().callEvent(combustEvent);

							if (!combustEvent.isCancelled()) {
								victim.setOnFire(combustEvent.getDuration());
							}
							// CraftBukkit end
						}

						if (world instanceof WorldServer && f5 > 2.0F) {
							int k = (int) ((double) f5 * 0.5D);

							((WorldServer) world).a(Particles.i, victim.locX(),
									victim.locY() + (double) (victim.getHeight() * 0.5F), victim.locZ(), k, 0.1D, 0.0D, 0.1D,
									0.2D);
						}
					}

					attacker.applyExhaustion(world.spigotConfig.combatExhaustion); // Spigot - Change to use configurable value
				} else {
					//sendSoundEffect(attacker, attacker.locX, attacker.locY, attacker.locZ, SoundEffects.fx, attacker.bK(), 1.0F, 1.0F); // Paper - send while respecting visibility
					if (onFire) {
						victim.extinguish();
					}
					// CraftBukkit start - resync on cancelled event
					if (attacker instanceof EntityPlayer) {
						((EntityPlayer) attacker).getBukkitEntity().updateInventory();
					}
					// CraftBukkit end
				}
			}
        }
    }
}

