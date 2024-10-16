package com.github.maxopoly.finale.misc.crossbow;

import com.github.maxopoly.finale.misc.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.List;

public class AntiAirMissileInstance {

	public enum InstanceState {

		STARTING,
		RUNNING,
		EXPLODING

	}

	private final AntiAirMissile antiAirMissile;
	private final Player shooter;

	private InstanceState state;

	private Location startLoc;
	private Location loc;
	private Vector dir;

	public AntiAirMissileInstance(AntiAirMissile antiAirMissile, Player shooter) {
		this.antiAirMissile = antiAirMissile;
		this.shooter = shooter;

		this.state = InstanceState.STARTING;

		Location eyeLoc = shooter.getEyeLocation();
		Vector eyeDir = eyeLoc.getDirection();
		this.startLoc = eyeLoc.clone().add(eyeDir);

		this.loc = this.startLoc.clone();
		this.dir = eyeDir.clone();
	}

	// true = finished, false = not finished
	public boolean progress() {
		switch (state) {
			case STARTING:
				Location eyeLoc = shooter.getEyeLocation();
				Vector eyeDir = eyeLoc.getDirection();
				this.startLoc = eyeLoc.clone().add(eyeDir);

				this.loc = this.startLoc.clone();
				this.dir = eyeDir.clone();

				this.state = InstanceState.RUNNING;
				return false;
			case RUNNING:
				double distanceSquared = this.startLoc.distanceSquared(this.loc);
				if (distanceSquared > antiAirMissile.getMaxRange() * antiAirMissile.getMaxRange()) {
					this.state = InstanceState.EXPLODING;
					return false;
				}

				Collection<LivingEntity> nearbyEntities = this.loc.getNearbyLivingEntities(antiAirMissile.getHomingRadius());
				LivingEntity nearestFlyingEntity = null;
				double nearbyDistance = Double.MAX_VALUE;
				for (LivingEntity nearbyEntity : nearbyEntities) {
					if (nearbyEntity.getUniqueId().equals(shooter.getUniqueId())) {
						continue;
					}
					if (nearbyEntity instanceof Player) {
						Player nearbyPlayer = (Player) nearbyEntity;
						if ((nearbyPlayer.isGliding() || nearbyPlayer.isFlying()) && this.loc.distanceSquared(nearbyPlayer.getLocation()) < nearbyDistance) {
							nearestFlyingEntity = nearbyPlayer;
						}
					} else if (nearbyEntity instanceof Phantom) {
						Phantom nearbyPhantom = (Phantom) nearbyEntity;
						if (!nearbyPhantom.isOnGround() && this.loc.distanceSquared(nearbyPhantom.getLocation()) < nearbyDistance) {
							nearestFlyingEntity = nearbyPhantom;
						}
					}
				}

				if (nearestFlyingEntity != null) {
					Vector toNearestFlyingEntity = nearestFlyingEntity.getLocation().clone().subtract(this.loc).toVector().normalize();
					Vector intermediate = toNearestFlyingEntity.clone().subtract(this.dir);
					Vector homingTurn = intermediate.multiply(antiAirMissile.getHomingStrength());
					Vector newDir = this.dir.clone().add(homingTurn);
					this.dir = newDir;
				} else {
					double gravity = antiAirMissile.getGravity();
					this.dir = this.dir.clone().subtract(new Vector(0, gravity, 0)).normalize();
				}

				Location newLoc = this.loc.clone().add(this.dir.clone().multiply(antiAirMissile.getSpeed()));

				ParticleUtil.line(this.loc, newLoc, (loc) -> {
					Block block = loc.getBlock();
					if (!block.isPassable()) {
						this.state = InstanceState.EXPLODING;
						return true;
					}

					Collection<LivingEntity> activationEntities = loc.getNearbyLivingEntities(antiAirMissile.getActivateRadius());
					if (!activationEntities.isEmpty()) {
						boolean notOnlyShooter = false;
						for (LivingEntity livingEntity : activationEntities) {
							if (!livingEntity.getUniqueId().equals(shooter.getUniqueId())) {
								notOnlyShooter = true;
								break;
							}
						}
						if (notOnlyShooter) {
							this.state = InstanceState.EXPLODING;
							return true;
						}
					}

					loc.getWorld().spawnParticle(Particle.CLOUD, loc, 1, 0, 0, 0, 0, null, true);
					loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 1, 0, 0, 0, 0, null, true);
					loc.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0, null, true);
					return false;
				}, 10);

				this.loc = newLoc;
				return false;
			case EXPLODING:
				double damageRadius = antiAirMissile.getDamageRadius();

				this.loc.getWorld().playSound(this.loc, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
				this.loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, this.loc, (int) (10 * damageRadius * damageRadius), damageRadius / 2, damageRadius / 2, damageRadius / 2, 0, null, true);
				this.loc.getWorld().spawnParticle(Particle.CLOUD, this.loc, (int) (10 * damageRadius * damageRadius), damageRadius, damageRadius, damageRadius, 0, null, true);
				this.loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, this.loc, (int) (20 * damageRadius * damageRadius), damageRadius, damageRadius, damageRadius, 0, null, true);
				this.loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, this.loc, (int) (20 * damageRadius * damageRadius), damageRadius, damageRadius, damageRadius, 0, null, true);
				this.loc.getWorld().spawnParticle(Particle.FLAME, this.loc, (int) (40 * damageRadius * damageRadius), damageRadius, damageRadius, damageRadius, 0, null, true);

				Collection<LivingEntity> damageEntities = this.loc.getNearbyLivingEntities(damageRadius);
				for (LivingEntity livingEntity : damageEntities) {
					double distance = livingEntity.getLocation().distance(this.loc);
					double damageRatio = distance / damageRadius;
					double reductionMultiplier = 1 - damageRatio;
					double damage = antiAirMissile.getDamage() * reductionMultiplier;

					livingEntity.damage(damage, shooter);

					double power = antiAirMissile.getPower();
					Vector knockbackDir = livingEntity.getLocation().clone().subtract(this.loc).toVector().normalize();
					Vector newVel = knockbackDir.clone().multiply(power);
					if ((livingEntity.isGliding() || !livingEntity.isOnGround())) {
						newVel.setY(-power);
					} else {
						newVel.setY(power * 0.5);
					}
					livingEntity.setVelocity(newVel);
					livingEntity.setVelocity(newVel);
				}
				return true;
			default:
				return false;
		}
	}

}
