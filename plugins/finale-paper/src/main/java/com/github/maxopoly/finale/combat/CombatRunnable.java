package com.github.maxopoly.finale.combat;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.combat.event.CritHitEvent;

import net.minecraft.server.v1_13_R2.DamageSource;
import net.minecraft.server.v1_13_R2.EnchantmentManager;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityArmorStand;
import net.minecraft.server.v1_13_R2.EntityComplexPart;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityLiving;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.EnumMonsterType;
import net.minecraft.server.v1_13_R2.GenericAttributes;
import net.minecraft.server.v1_13_R2.IComplex;
import net.minecraft.server.v1_13_R2.ItemStack;
import net.minecraft.server.v1_13_R2.ItemSword;
import net.minecraft.server.v1_13_R2.MathHelper;
import net.minecraft.server.v1_13_R2.MobEffects;
import net.minecraft.server.v1_13_R2.PacketPlayOutEntityVelocity;
import net.minecraft.server.v1_13_R2.Particles;
import net.minecraft.server.v1_13_R2.SoundEffects;
import net.minecraft.server.v1_13_R2.StatisticList;
import net.minecraft.server.v1_13_R2.World;
import net.minecraft.server.v1_13_R2.WorldServer;

public class CombatRunnable implements Runnable {

	private Queue<Hit> queuedHits = new ConcurrentLinkedQueue<>();
	
	public Queue<Hit> getHitQueue() {
		return queuedHits;
	}
	
	@Override
	public void run() {
		while (!queuedHits.isEmpty()) {
        	Hit hit = queuedHits.poll();
        	
        	EntityPlayer attacker = ((CraftPlayer)hit.getAttacker()).getHandle();
        	EntityLiving victim = ((CraftLivingEntity)hit.getVictim()).getHandle();
        	
        	attack(attacker, victim);
        }
	}
	
	public static void attack(EntityHuman attacker, Entity entity) {
		CombatConfig config = Finale.getPlugin().getManager().getCombatConfig();
        if (entity.bk()) {
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
	                f2 = attacker.r(0.5F);
	
	                f *= 0.2F + f2 * f2 * 0.8F;
	                f1 *= f2;
                }
                World world = attacker.getWorld();
                attacker.dH();
                if (f > 0.0F || f1 > 0.0F) {
                	boolean flag = f2 > 0.9F;
                    byte b0 = 0;
                    boolean flag1 = false;
                    int i = b0 + EnchantmentManager.b((EntityLiving) attacker);

                    if (attacker.isSprinting() && flag) {
                    	if (config.getCombatSounds().isKnockbackEnabled()) {
                    		attacker.world.a(attacker, attacker.locX, attacker.locY, attacker.locZ, SoundEffects.ENTITY_PLAYER_ATTACK_KNOCKBACK, attacker.bV(), 1.0F, 1.0F); // Paper - send while respecting visibility
                    	}
                        ++i;
                        flag1 = true;
                    }

                    boolean flag2 = attacker.fallDistance > 0.0F && !attacker.onGround && !attacker.z_() && !attacker.isInWater() && !attacker.hasEffect(MobEffects.BLINDNESS) && !attacker.isPassenger() && entity instanceof EntityLiving;
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

                    if (!flag2 && !attacker.isSprinting() && attacker.onGround && d0 < (double) attacker.cK()) {
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

                    double d1 = entity.motX;
                    double d2 = entity.motY;
                    double d3 = entity.motZ;
                    boolean flag5 = entity.damageEntity(DamageSource.playerAttack(attacker), f);

                    if (flag5) {
                        if (i > 0) {
                        	entity.f((double) (-MathHelper.sin(attacker.yaw * 3.1415927F / 180.0F) * (float) i * 0.5F) * config.getHorizontalKB(),
                        			0.1D * config.getVerticalKB(),
                        			(double) (MathHelper.cos(attacker.yaw * 3.1415927F / 180.0F) * (float) i * 0.5F) * config.getHorizontalKB());

                        	attacker.motX *= 0.6D;
                        	attacker.motZ *= 0.6D;
                        	attacker.setSprinting(false);
                        }
                        
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

                            attacker.world.a(attacker, attacker.locX, attacker.locY, attacker.locZ, SoundEffects.ENTITY_PLAYER_ATTACK_SWEEP, attacker.bV(), 1.0F, 1.0F); // Paper - send while respecting visibility
                            attacker.dl();
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
                            entity.motX = d1;
                            entity.motY = d2;
                            entity.motZ = d3;
                            }
                            // CraftBukkit end
                        }

                        if (flag2) {
                        	if (config.getCombatSounds().isCritEnabled()) {
	                        	attacker.world.a((EntityHuman) null, attacker.locX, attacker.locY, attacker.locZ,
	    								SoundEffects.ENTITY_PLAYER_ATTACK_CRIT, attacker.bV(), 1.0F, 1.0F);
                        	}
                            attacker.a(entity);
                        }
                        
                        if (!flag2 && !flag3) {
                            if (flag && config.getCombatSounds().isStrongEnabled()) {
                            	attacker.world.a(attacker, attacker.locX, attacker.locY, attacker.locZ, SoundEffects.ENTITY_PLAYER_ATTACK_STRONG, attacker.bV(), 1.0F, 1.0F); // Paper - send while respecting visibility
                            } else if (config.getCombatSounds().isWeakEnabled()) {
                            	attacker.world.a(attacker, attacker.locX, attacker.locY, attacker.locZ, SoundEffects.ENTITY_PLAYER_ATTACK_WEAK, attacker.bV(), 1.0F, 1.0F); // Paper - send while respecting visibility
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
                            IComplex icomplex = ((EntityComplexPart) entity).owner;

                            if (icomplex instanceof EntityLiving) {
                                object = (EntityLiving) icomplex;
                            }
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

                                ((WorldServer) world).a(Particles.i, entity.locX,
    									entity.locY + (double) (entity.length * 0.5F), entity.locZ, k, 0.1D, 0.0D, 0.1D,
    									0.2D);
                            }
                        }

                        attacker.applyExhaustion(world.spigotConfig.combatExhaustion); // Spigot - Change to use configurable value
                    } else {
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
