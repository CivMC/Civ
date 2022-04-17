package com.github.maxopoly.finale.misc;

import com.github.maxopoly.finale.Finale;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;

public class ShieldHandler {

	private boolean shieldBashEnabled;
	private boolean shieldBashResistanceEnabled;
	private int shieldBashResistanceAmplifier;
	private int shieldBashResistanceDuration;
	private Vector shieldBashPowerFromGround;
	private Vector shieldBashPowerInAir;
	private int shieldBashTargetDistance;
	private double shieldBashDamage;
	private CooldownHandler shieldBashCooldowns;

	private boolean passiveResistanceEnabled;
	private int passiveResistanceAmplifier;

	public ShieldHandler(boolean shieldBashEnabled, boolean shieldBashResistanceEnabled, int shieldBashResistanceAmplifier,
						 int shieldBashResistanceDuration, Vector shieldBashPowerFromGround, Vector shieldBashPowerInAir,
						 int shieldBashTargetDistance, double shieldBashDamage,
						 long shieldBashCooldown, boolean passiveResistanceEnabled, int passiveResistanceAmplifier) {
		this.shieldBashEnabled = shieldBashEnabled;
		this.shieldBashResistanceEnabled = shieldBashResistanceEnabled;
		this.shieldBashResistanceAmplifier = shieldBashResistanceAmplifier;
		this.shieldBashResistanceDuration = shieldBashResistanceDuration;
		this.shieldBashPowerFromGround = shieldBashPowerFromGround;
		this.shieldBashPowerInAir = shieldBashPowerInAir;
		this.shieldBashTargetDistance = shieldBashTargetDistance;
		this.shieldBashDamage = shieldBashDamage;
		this.shieldBashCooldowns = new CooldownHandler("shieldBashCooldown", shieldBashCooldown, (player, cooldowns) ->
			ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Shield: " +
					ChatColor.GRAY + CooldownHandler.formatCoolDown(cooldowns, player.getUniqueId())
		);

		this.passiveResistanceEnabled = passiveResistanceEnabled;
		this.passiveResistanceAmplifier = passiveResistanceAmplifier;
	}

	public void activateShieldBash(Player player) {
		if (!shieldBashEnabled) {
			return;
		}

		if (shieldBashCooldowns.onCooldown(player)) {
			return;
		}

		if (shieldBashResistanceEnabled) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, shieldBashResistanceDuration, shieldBashResistanceAmplifier));
		}

		Location location = player.getLocation();
		location.getWorld().spawnParticle(Particle.CLOUD, location, 5);

		Vector shieldBashPower = shieldBashPowerFromGround.clone();
		if (!player.isOnGround()) {
			shieldBashPower = shieldBashPowerInAir.clone();
		}

		Vector direction = player.getEyeLocation().getDirection();
		player.setVelocity(direction.clone().multiply(shieldBashPower));

		Entity targetEntity = player.getTargetEntity(shieldBashTargetDistance);
		if (targetEntity != null) {
			targetEntity.setVelocity(direction.clone().multiply(shieldBashPower));
			if (targetEntity instanceof LivingEntity) {
				LivingEntity livingTargetEntity = (LivingEntity) targetEntity;
				livingTargetEntity.damage(shieldBashDamage, player);
			}
		}

		shieldBashCooldowns.putOnCooldown(player);
	}

	public boolean isPassiveResistanceEnabled() {
		return passiveResistanceEnabled;
	}

	public int getPassiveResistanceAmplifier() {
		return passiveResistanceAmplifier;
	}
}
