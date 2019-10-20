package com.github.maxopoly.finale.combat;

import java.util.Iterator;
import java.util.List;

import org.bukkit.craftbukkit.v1_14_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.combat.event.CritHitEvent;

import net.minecraft.server.v1_14_R1.DamageSource;
import net.minecraft.server.v1_14_R1.EnchantmentManager;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityArmorStand;
import net.minecraft.server.v1_14_R1.EntityComplexPart;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.EnumMonsterType;
import net.minecraft.server.v1_14_R1.GenericAttributes;
import net.minecraft.server.v1_14_R1.ItemStack;
import net.minecraft.server.v1_14_R1.ItemSword;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.MobEffects;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityVelocity;
import net.minecraft.server.v1_14_R1.PacketPlayOutNamedSoundEffect;
import net.minecraft.server.v1_14_R1.Particles;
import net.minecraft.server.v1_14_R1.SoundCategory;
import net.minecraft.server.v1_14_R1.SoundEffect;
import net.minecraft.server.v1_14_R1.SoundEffects;
import net.minecraft.server.v1_14_R1.StatisticList;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.minecraft.server.v1_14_R1.WorldServer;

public class CombatUtil {
	
	 private static void sendSoundEffect(EntityHuman fromEntity, double x, double y, double z, SoundEffect soundEffect, SoundCategory soundCategory, float volume, float pitch) {
        fromEntity.world.playSound(fromEntity, x, y, z, soundEffect, soundCategory, volume, pitch); // This will not send the effect to the entity himself
        if (fromEntity instanceof EntityPlayer) {
            ((EntityPlayer) fromEntity).playerConnection.sendPacket(new PacketPlayOutNamedSoundEffect(soundEffect, soundCategory, x, y, z, volume, pitch));
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
	
	public static void attack(EntityHuman attacker, Entity entity) {
		CombatConfig config = Finale.getPlugin().getManager().getCombatConfig();
        if (entity.bs()) {
            if (!entity.t(attacker)) {
                float f = (float) attacker.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue();
                float f1;

                if (entity instanceof EntityLiving) {
                    f1 = EnchantmentManager.a(attacker.getItemInMainHand(), ((EntityLiving) entity).getMonsterType());
                } else {
                    f1 = EnchantmentManager.a(attacker.getItemInMainHand(), EnumMonsterType.UNDEFINED);
                }
                
                float f2 = 1;
                if (!config.isNoCooldown()) {
                	f2 = attacker.s(0.5F);
	                f *= 0.2F + f2 * f2 * 0.8F;
	                f1 *= f2;
                }
                World world = attacker.getWorld();
                attacker.dZ();
                if (f > 0.0F || f1 > 0.0F) {
                	boolean flag = f2 > 0.9F;
                    byte b0 = 0;
                    boolean flag1 = false;
                    int i = b0 + EnchantmentManager.b((EntityLiving) attacker);
                    boolean sprinting = attacker.isSprinting();

                    if (sprinting && flag) {
                    	if (config.getCombatSounds().isKnockbackEnabled()) {
                    		sendSoundEffect(attacker, attacker.locX, attacker.locY, attacker.locZ, SoundEffects.ENTITY_PLAYER_ATTACK_KNOCKBACK, attacker.getSoundCategory(), 1.0F, 1.0F); // Paper - send while respecting visibility
                    	}
                        ++i;
                        flag1 = true;
                    }

                    boolean flag2 = attacker.fallDistance > 0.0F && !attacker.onGround && !attacker.isClimbing() && !attacker.isInWater() && !attacker.hasEffect(MobEffects.BLINDNESS) && !attacker.isPassenger() && entity instanceof EntityLiving;
                    flag2 = flag2 && !attacker.isSprinting();
                    if (flag2) {
                    	double critMultiplier = 1.5d;
                    	if ((attacker.getBukkitEntity() instanceof Player) && (entity.getBukkitEntity() instanceof LivingEntity)) {
                    		CritHitEvent critHitEvent = new CritHitEvent(((Player) attacker.getBukkitEntity()), ((LivingEntity) entity.getBukkitEntity()), critMultiplier);
                    		org.bukkit.Bukkit.getPluginManager().callEvent(critHitEvent);
                    		
                    		critMultiplier = critHitEvent.getCritMultiplier();
                    	}
                    	
                        f *= critMultiplier;
                    }
                    f += f1;
                    boolean flag3 = false;
                    double d0 = (double) (attacker.K - attacker.J);

                    if (!flag2 && !attacker.isSprinting() && attacker.onGround && d0 < (double) attacker.db()) {
                        ItemStack itemstack = attacker.b(EnumHand.MAIN_HAND);

                        if (itemstack.getItem() instanceof ItemSword) {
                            flag3 = true;
                        }
                    }

                    float f3 = 0.0F;
                    boolean flag4 = false;
                    int j = EnchantmentManager.getFireAspectEnchantmentLevel(attacker);

                    if (entity instanceof EntityLiving) {
                        f3 = ((EntityLiving) entity).getHealth();
                        if (j > 0 && !entity.isBurning()) {
                            // CraftBukkit start - Call a combust event when somebody hits with a fire enchanted item
                            EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(attacker.getBukkitEntity(), entity.getBukkitEntity(), 1);
                            org.bukkit.Bukkit.getPluginManager().callEvent(combustEvent);

                            if (!combustEvent.isCancelled()) {
                                flag4 = true;
                                entity.setOnFire(combustEvent.getDuration());
                            }
                            // CraftBukkit end
                        }
                    }

                    Vec3D mot = entity.getMot();
                    double d1 = mot.x;
                    double d2 = mot.y;
                    double d3 = mot.z;
                    boolean flag5 = entity.damageEntity(DamageSource.playerAttack(attacker), f);

                    if (flag5) {
                    	double x = (double) (-MathHelper.sin(attacker.yaw * 3.1415927F / 180.0F) * 0.5F) * i * config.getHorizontalKB();
                    	double y = 0.1D * config.getVerticalKB();
                    	double z = (double) (MathHelper.cos(attacker.yaw * 3.1415927F / 180.0F) * 0.5F) * i * config.getHorizontalKB();
                    	if (sprinting) {
                    		x *= config.getSprintHorizontal();
                    		y *= config.getSprintVertical();
                    		z *= config.getSprintHorizontal();
                    	}
                    	if (!entity.onGround) {
                    		x *= config.getAirHorizontal();
                    		y *= config.getAirVertical();
                    		z *= config.getAirHorizontal();
                    	}
                    	if (entity.isInWater()) {
                    		x *= config.getWaterHorizontal();
                    		y *= config.getWaterVertical();
                    		z *= config.getWaterHorizontal();
                    	}
                    	entity.f(x, y, z);

                    	attacker.setMot(attacker.getMot().d(config.getAttackMotionModifier(), 1.0, config.getAttackMotionModifier()));
                    	attacker.setSprinting(!config.isStopSprinting());
                    	
                    	//((CraftPlayer)attacker.getBukkitEntity()).sendMessage("motX: " + entity.motX + ", motY: " + entity.motY + ", motZ: " + entity.motZ + ", onGround: " + entity.onGround);
                        
                        if (flag3 && config.isSweepEnabled()) {
                            float f4 = 1.0F + EnchantmentManager.a((EntityLiving) attacker) * f;
                            List<EntityLiving> list = attacker.world.a(EntityLiving.class, entity.getBoundingBox().grow(1.0D, 0.25D, 1.0D));
                            Iterator iterator = list.iterator();

                            while (iterator.hasNext()) {
                                EntityLiving entityliving = (EntityLiving) iterator.next();

                                if (entityliving != attacker && entityliving != entity && !attacker.r(entityliving) && (!(entityliving instanceof EntityArmorStand) || !((EntityArmorStand) entityliving).isMarker()) && attacker.h(entityliving) < 9.0D) {
                                    // CraftBukkit start - Only apply knockback if the damage hits
                                    if (entityliving.damageEntity(DamageSource.playerAttack(attacker).sweep(), f4)) {
                                    entityliving.a(attacker, 0.4F, (double) MathHelper.sin(attacker.yaw * 0.017453292F), (double) (-MathHelper.cos(attacker.yaw * 0.017453292F)));
                                    }
                                    // CraftBukkit end
                                }
                            }

                            attacker.world.playSound(attacker, attacker.locX, attacker.locY, attacker.locZ, SoundEffects.ENTITY_PLAYER_ATTACK_SWEEP, attacker.getSoundCategory(), 1.0F, 1.0F); // Paper - send while respecting visibility
                            attacker.dE();
                        }

                        if (entity instanceof EntityPlayer && entity.velocityChanged) {
                            boolean cancelled = false;
                            Player player = (Player) entity.getBukkitEntity();
                            org.bukkit.util.Vector velocity = new Vector( d1, d2, d3 );

                            PlayerVelocityEvent event = new PlayerVelocityEvent(player, velocity.clone());
                            world.getServer().getPluginManager().callEvent(event);

                            if (event.isCancelled()) {
                                cancelled = true;
                            } else if (!velocity.equals(event.getVelocity())) {
                                player.setVelocity(event.getVelocity());
                            }

                            if (!cancelled) {
	                            ((EntityPlayer) entity).playerConnection.sendPacket(new PacketPlayOutEntityVelocity(entity));
	                            entity.velocityChanged = false;
	                            entity.setMot(d1, d2, d3);
                            }
                            // CraftBukkit end
                        }

                        if (flag2) {
                        	if (config.getCombatSounds().isCritEnabled()) {
	                        	sendSoundEffect(attacker, attacker.locX, attacker.locY, attacker.locZ,
	    								SoundEffects.ENTITY_PLAYER_ATTACK_CRIT, attacker.getSoundCategory(), 1.0F, 1.0F);
                        	}
                            attacker.a(entity);
                        }
                        
                        if (!flag2 && !flag3) {
                            if (flag && config.getCombatSounds().isStrongEnabled()) {
                            	attacker.world.playSound(attacker, attacker.locX, attacker.locY, attacker.locZ, SoundEffects.ENTITY_PLAYER_ATTACK_STRONG, attacker.getSoundCategory(), 1.0F, 1.0F); // Paper - send while respecting visibility
                            } else if (config.getCombatSounds().isWeakEnabled()) {
                            	attacker.world.playSound(attacker, attacker.locX, attacker.locY, attacker.locZ, SoundEffects.ENTITY_PLAYER_ATTACK_WEAK, attacker.getSoundCategory(), 1.0F, 1.0F); // Paper - send while respecting visibility
                            }
                        }
                        
                        if (f1 > 0.0F) {
                        	attacker.b(entity);
                        }
                        
                        attacker.z(entity);
                        if (entity instanceof EntityLiving) {
                            EnchantmentManager.a((EntityLiving) entity, (Entity) attacker);
                        }

                        EnchantmentManager.b((EntityLiving) attacker, entity);
                        ItemStack itemstack1 = attacker.getItemInMainHand();
                        Object object = entity;

                        if (entity instanceof EntityComplexPart) {
                        	object = ((EntityComplexPart) entity).owner;
                        }

                        if (!itemstack1.isEmpty() && object instanceof EntityLiving) {
                            itemstack1.a((EntityLiving) object, attacker);
                            if (itemstack1.isEmpty()) {
                            	attacker.a(EnumHand.MAIN_HAND, ItemStack.a);
                            }
                        }

                        if (entity instanceof EntityLiving) {
                            float f5 = f3 - ((EntityLiving) entity).getHealth();
                            
                            attacker.a(StatisticList.DAMAGE_DEALT, Math.round(f5 * 10.0F));
                            if (j > 0) {
                                // CraftBukkit start - Call a combust event when somebody hits with a fire enchanted item
                                EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(attacker.getBukkitEntity(), entity.getBukkitEntity(), j * 4);
                                org.bukkit.Bukkit.getPluginManager().callEvent(combustEvent);

                                if (!combustEvent.isCancelled()) {
                                    entity.setOnFire(combustEvent.getDuration());
                                }
                                // CraftBukkit end
                            }

                            if (world instanceof WorldServer && f5 > 2.0F) {
                                int k = (int) ((double) f5 * 0.5D);

                                ((WorldServer) world).a(Particles.DAMAGE_INDICATOR, entity.locX,
    									entity.locY + (double) (entity.getHeight() * 0.5F), entity.locZ, k, 0.1D, 0.0D, 0.1D,
    									0.2D);
                            }
                        }

                        attacker.applyExhaustion(world.spigotConfig.combatExhaustion); // Spigot - Change to use configurable value
                    } else {
                    	//sendSoundEffect(attacker, attacker.locX, attacker.locY, attacker.locZ, SoundEffects.fx, attacker.bK(), 1.0F, 1.0F); // Paper - send while respecting visibility
                        if (flag4) {
                            entity.extinguish();
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
}

