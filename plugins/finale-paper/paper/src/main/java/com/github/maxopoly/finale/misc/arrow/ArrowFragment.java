package com.github.maxopoly.finale.misc.arrow;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.misc.ParticleUtil;
import com.github.maxopoly.finale.misc.ally.AllyHandler;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ArrowFragment {

	private final ArrowHandler arrowHandler;
	private final Arrow arrow;
	private final ProjectileHitEvent event;

	private final Particle.DustOptions dustOptions;

	private Vector dir;
	private Location startLoc;
	private Location loc;

	public ArrowFragment(ArrowHandler arrowHandler, Arrow arrow, ProjectileHitEvent event, Location startLoc, Vector dir) {
		this.arrowHandler = arrowHandler;
		this.arrow = arrow;
		this.event = event;

		this.startLoc = startLoc.clone();
		this.loc = startLoc;
		this.dir = dir;

		Color color;
		try {
			color = arrow.getColor();
		} catch (Exception e) {
			color = null;
		}
		if (color == null) {
			color = Color.fromRGB(87, 87, 87);
		}
		dustOptions = new Particle.DustOptions(color, 1);
	}

	public boolean progress() {
		if (loc.distanceSquared(startLoc) > arrowHandler.getRadius() * arrowHandler.getRadius()) {
			return true;
		}

		Location newLoc = loc.clone().add(dir);

		if (ParticleUtil.line(this.loc, newLoc, (loc) -> {
			if (!loc.getBlock().isPassable()) {
				return true;
			}

			Collection<LivingEntity> nearbyEntities = loc.getNearbyLivingEntities(0.5);
			if (!nearbyEntities.isEmpty()) {
				for (LivingEntity nearby : nearbyEntities) {
					if (event.getHitEntity() != null && event.getHitEntity().getUniqueId().equals(nearby.getUniqueId())) {
						continue;
					}

					double localDamage = arrowHandler.getDamage();
					List<PotionEffect> effects = arrow.getCustomEffects();

					if (nearby instanceof Player) {
						Player nearbyPlayer = (Player) nearby;
						Player shooter = (Player) arrow.getShooter();
						AllyHandler allyHandler = Finale.getPlugin().getManager().getAllyHandler();
						if (allyHandler.isAllyOf(shooter, nearbyPlayer)) {
							localDamage *= (1 - arrowHandler.getAllyDamageReduction());

							if (!arrowHandler.getAllyExemptPotionTypes().contains(arrow.getBasePotionData().getType()) &&
									!effects.isEmpty() && arrowHandler.getAllyDamageReduction() < 1) {
								List<PotionEffect> newEffects = new ArrayList<>();
								for (PotionEffect effect : effects) {
									newEffects.add(new PotionEffect(effect.getType(), (int) (effect.getDuration() * arrowHandler.getAllyDamageReduction()), effect.getAmplifier()));
								}
								effects = newEffects;
							}
						}
					}

					if (localDamage > 0) {
						nearby.damage(localDamage, arrow);
						nearby.addPotionEffects(effects);
					}
				}
			}

			loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, dustOptions, true);
			return false;
		}, 3)) {
			return true;
		}

		this.loc = newLoc;
		return false;
	}

}
