package com.github.maxopoly.finale.combat;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.combat.event.CritHitEvent;
import com.github.maxopoly.finale.combat.knockback.KnockbackStrategy;
import com.github.maxopoly.finale.misc.knockback.KnockbackConfig;

import java.util.Iterator;
import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.CraftVector;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

public class CombatUtil {

    public static org.bukkit.inventory.ItemStack DAMAGING_ITEM = null;

    private static void sendSoundEffect(net.minecraft.world.entity.player.Player fromEntity, double x, double y, double z, SoundEvent soundEffect, SoundSource soundCategory, float volume, float pitch) {
        fromEntity.playSound(soundEffect, volume, pitch); // This will not send the effect to the entity himself
        if (fromEntity instanceof ServerPlayer) {
            ((ServerPlayer) fromEntity).connection.send(new ClientboundSoundPacket(Holder.direct(soundEffect), soundCategory, x, y, z, volume, pitch, fromEntity.level().getRandom().nextLong()));
        }
    }

    public static void attack(Player attacker, Entity victim) {
        attack(((CraftPlayer) attacker).getHandle(), victim);
    }

    //see net.minecraft.world.entity.player.Player#attack(Entity) to update this
    public static void attack(ServerPlayer attacker, Entity victim) {
        CombatConfig config = Finale.getPlugin().getManager().getCombatConfig();
        SprintHandler sprintHandler = Finale.getPlugin().getManager().getSprintHandler();
        if (victim.isAttackable() && !victim.skipAttackInteraction(attacker)) {
            float damage = (float) attacker.getAttribute(Attributes.ATTACK_DAMAGE).getValue();
            DamageSource damagesource = attacker.damageSources().playerAttack(attacker);
            float f1 = EnchantmentHelper.modifyDamage(attacker.serverLevel(), attacker.getWeaponItem(), victim, damagesource, damage) - damage;

            float f2;
            boolean shouldDamage = true;
            if (config.isAttackCooldownEnabled()) {
                f2 = attacker.getAttackStrengthScale(0.5F);
                shouldDamage = f2 > 0.9f;
                damage *= 0.2F + f2 * f2 * 0.8F;
                f1 *= f2;
            }
            Level world = attacker.level();
            if (damage > 0.0F || f1 > 0.0F) {
                boolean dealtExtraKnockback = false;
                byte baseKnockbackLevel = 1;
                float knockbackLevel = baseKnockbackLevel + EnchantmentHelper.modifyKnockback(attacker.serverLevel(), attacker.getWeaponItem(), victim, damagesource, (float) attacker.getAttributeValue(Attributes.ATTACK_KNOCKBACK));

                if (sprintHandler.isSprinting(attacker) && shouldDamage) {
                    if (config.getCombatSounds().isKnockbackEnabled()) {
                        sendSoundEffect(attacker, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, attacker.getSoundSource(), 1.0F, 1.0F); // Paper - send while respecting visibility
                    }
                    if (!config.isKnockbackSwordsEnabled()) {
                        ++knockbackLevel;
                    }
                    dealtExtraKnockback = true;
                }

                boolean shouldCrit = shouldDamage && attacker.fallDistance > 0.0F && !attacker.onGround() && !attacker.onClimbable() && !attacker.isInWater()
                    && !attacker.hasEffect(MobEffects.BLINDNESS) && !attacker.isPassenger() && victim instanceof LivingEntity;
                shouldCrit = shouldCrit && !sprintHandler.isSprinting(attacker);
                if (shouldCrit) {
                    double critMultiplier = 1.5d;
                    if (victim.getBukkitEntity() instanceof org.bukkit.entity.LivingEntity) {
                        CritHitEvent critHitEvent = new CritHitEvent(((Player) attacker.getBukkitEntity()), ((org.bukkit.entity.LivingEntity) victim.getBukkitEntity()), critMultiplier);
                        org.bukkit.Bukkit.getPluginManager().callEvent(critHitEvent);

                        critMultiplier = critHitEvent.getCritMultiplier();
                    }

                    damage *= critMultiplier;
                }
                damage += f1;
                boolean shouldSweep = false;
                double d0 = attacker.getKnownMovement().horizontalDistanceSqr();
                double d1 = (double) attacker.getSpeed() * 2.5D;

                if (shouldDamage && !shouldCrit && !dealtExtraKnockback && attacker.onGround() && d0 < Mth.square(d1)) {
                    ItemStack itemstack = attacker.getItemInHand(InteractionHand.MAIN_HAND);
                    if (itemstack.getItem() instanceof SwordItem) {
                        shouldSweep = true;
                    }
                }

                float victimHealth = 0.0F;

                Vec3 victimMot = victim.getDeltaMovement();
                if (shouldDamage) {
                    boolean damagedVictim;
                    boolean wasInvulnerable = victim instanceof LivingEntity living && living.invulnerableTime > (float) living.invulnerableDuration / 2.0F && !damagesource.is(DamageTypeTags.BYPASSES_COOLDOWN);
                    try {
                        DAMAGING_ITEM = attacker.getBukkitEntity().getInventory().getItemInMainHand();
                        damagedVictim = victim.hurtServer(victim.level().getMinecraftWorld(), damagesource, damage);
                    } finally {
                        DAMAGING_ITEM = null;
                    }
                    if (damagedVictim) {
                        if ((knockbackLevel > 0 || dealtExtraKnockback)) {
                            if (victim instanceof LivingEntity living) {
                                if (!wasInvulnerable) {
                                    KnockbackStrategy knockbackStrategy = config.getKnockbackStrategy();

                                    knockbackStrategy.handleKnockback(attacker, living, knockbackLevel);
                                }
                            }
                        }

                        //((CraftPlayer)attacker.getBukkitEntity()).sendMessage("motX: " + victim.motX + ", motY: " + victim.motY + ", motZ: " + victim.motZ + ", onGround: " + victim.onGround);

                        if (shouldSweep && config.isSweepEnabled()) {
                            float f4 = 1.0F + (float) attacker.getAttributeValue(Attributes.SWEEPING_DAMAGE_RATIO) * damage;
                            List<LivingEntity> list = attacker.level().getEntitiesOfClass(LivingEntity.class, victim.getBoundingBox().inflate(1.0D, 0.25D, 1.0D));
                            Iterator<LivingEntity> iterator = list.iterator();

                            while (iterator.hasNext()) {
                                LivingEntity entityliving = iterator.next();

                                if (entityliving != attacker && entityliving != victim && !attacker.skipAttackInteraction(entityliving) && (!(entityliving instanceof ArmorStand) || !((ArmorStand) entityliving).isMarker()) && attacker.distanceToSqr(entityliving) < 9.0D) {
                                    // CraftBukkit start - Only apply knockback if the damage hits
                                    if (entityliving.hurtServer(entityliving.level().getMinecraftWorld(), world.damageSources().playerAttack(attacker).sweep(), f4)) {
                                        entityliving.knockback(0.4F, (double) Mth.sin(attacker.getBukkitYaw() * 0.017453292F), (double) (-Mth.cos(attacker.getBukkitYaw() * 0.017453292F)));
                                    }
                                    // CraftBukkit end

                                    EnchantmentHelper.doPostAttackEffects(attacker.serverLevel(), victim, damagesource);
                                }
                            }

                            world.playSound(attacker, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, attacker.getSoundSource(), 1.0F, 1.0F); // Paper - send while respecting visibility
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
                            if (shouldDamage && config.getCombatSounds().isStrongEnabled()) {
                                attacker.level().playSound(attacker, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, attacker.getSoundSource(), 1.0F, 1.0F); // Paper - send while respecting visibility
                            } else if (config.getCombatSounds().isWeakEnabled()) {
                                attacker.level().playSound(attacker, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, attacker.getSoundSource(), 1.0F, 1.0F); // Paper - send while respecting visibility
                            }
                        }

                        if (f1 > 0.0F) {
                            attacker.magicCrit(victim);
                        }

                        InteractionHand hand = attacker.swingingArm != null ? attacker.swingingArm : InteractionHand.MAIN_HAND;
                        ItemStack itemstack1 = attacker.getItemInHand(hand);
                        Object object = victim;

                        if (victim instanceof EnderDragonPart) {
                            object = ((EnderDragonPart) victim).parentMob;
                        }

                        boolean doPostHurtEnemy = false;

                        attacker.setLastHurtMob(victim);
                        if (victim instanceof LivingEntity livingVictim) {
                            doPostHurtEnemy = attacker.getWeaponItem().hurtEnemy(livingVictim, attacker);
                        }

                        EnchantmentHelper.doPostAttackEffects(attacker.serverLevel(), victim, damagesource);

                        if (!itemstack1.isEmpty() && object instanceof LivingEntity) {
                            if (doPostHurtEnemy) {
                                itemstack1.postHurtEnemy((LivingEntity) object, attacker);
                            }
                            if (itemstack1.isEmpty()) {
                                if (itemstack1 == attacker.getMainHandItem()) {
                                    attacker.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                                } else {
                                    attacker.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                                }
                            }
                        }

                        if (victim instanceof LivingEntity) {
                            float f5 = victimHealth - ((LivingEntity) victim).getHealth();

                            attacker.awardStat(Stats.DAMAGE_DEALT, Math.round(f5 * 10.0F));

                            if (world instanceof ServerLevel && f5 > 2.0F) {
                                int k = (int) ((double) f5 * 0.5D);

                                ((ServerLevel) world).sendParticles(ParticleTypes.DAMAGE_INDICATOR, victim.getX(),
                                    victim.getY() + (double) (victim.getEyeHeight() * 0.5F), victim.getZ(), k, 0.1D, 0.0D, 0.1D,
                                    0.2D);
                            }
                        }

                        attacker.causeFoodExhaustion(world.spigotConfig.combatExhaustion, EntityExhaustionEvent.ExhaustionReason.ATTACK); // Spigot - Change to use configurable value
                    } else {
                        //sendSoundEffect(attacker, attacker.locX, attacker.locY, attacker.locZ, SoundEffects.fx, attacker.bK(), 1.0F, 1.0F); // Paper - send while respecting visibility
                        // CraftBukkit start - resync on cancelled event
                        //noinspection deprecation
                        attacker.getBukkitEntity().updateInventory();
                        // CraftBukkit end
                    }
                }
            }
        }
    }
}

