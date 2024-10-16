package com.github.maxopoly.finale.misc.arrow;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.misc.ally.AllyHandler;
import com.github.maxopoly.finale.misc.arrow.ArrowFragment;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TippedArrow;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.*;

public class ArrowHandler {

	private boolean enabled;
	private double minDistance;
	private double radius;
	private double damage;
	private double allyDamageReduction;
	private boolean allyCollide;
	private Set<PotionType> allyExemptArrowTypes;

	private List<ArrowFragment> fragments = new ArrayList<>();

	public ArrowHandler(boolean enabled, double minDistance, double radius, double damage, double allyDamageReduction, boolean allyCollide, Set<PotionType> allyExemptArrowTypes) {
		this.enabled = enabled;
		this.minDistance = minDistance;
		this.radius = radius;
		this.damage = damage;
		this.allyDamageReduction = allyDamageReduction;
		this.allyCollide = allyCollide;
		this.allyExemptArrowTypes = allyExemptArrowTypes;

		new BukkitRunnable() {

			@Override
			public void run() {
				progressFragments();
			}

		}.runTaskTimer(Finale.getPlugin(), 0L, 1L);
	}

	public void progressFragments() {
		if (fragments.isEmpty()) {
			return;
		}

		Iterator<ArrowFragment> arrowFragmentIt = fragments.iterator();
		while (arrowFragmentIt.hasNext()) {
			ArrowFragment fragment = arrowFragmentIt.next();
			if (fragment.progress()) {
				arrowFragmentIt.remove();
			}
		}
	}

	private Random random = new Random();
	public void arrowImpact(ProjectileHitEvent event) {
		if (!enabled) {
			return;
		}

		Arrow arrow = (Arrow) event.getEntity();
		if (arrow.getItemStack().getType() != Material.TIPPED_ARROW) {
			return;
		}

		if (!(arrow.getShooter() instanceof Player)) {
			return;
		}

		Player shooter = (Player) arrow.getShooter();
		Location landedLocation = null;
		if (event.getHitBlock() != null) {
			landedLocation = event.getHitBlock().getLocation();
		}
		if (event.getHitEntity() != null) {
			landedLocation = event.getHitEntity().getLocation();
		}
		if (landedLocation == null) {
			return;
		}

		if (shooter.getLocation().distanceSquared(landedLocation) < minDistance * minDistance) {
			return;
		}

		landedLocation = landedLocation.add(arrow.getVelocity().multiply(-1).normalize().multiply(1.5));
		//landedLocation.getWorld().spawnParticle(Particle.FLAME, landedLocation, 1, 0, 0, 0, 0, null, true);
		landedLocation.getWorld().playSound(landedLocation, Sound.BLOCK_GLASS_BREAK, 1f, 0.5f);

		int increment = 10;
		for (double theta = 0; theta < 180; theta += increment) {
			for (double phi = 0; phi < 360; phi += increment) {
				if (random.nextInt(3) == 0) {
					final double rphi = Math.toRadians(phi);
					final double rtheta = Math.toRadians(theta);

					Vector dir = new Vector(Math.cos(rphi) * Math.sin(rtheta), Math.cos(rtheta), Math.sin(rphi) * Math.sin(rtheta)).normalize();
					final Location start = landedLocation.clone().add(dir);
					fragments.add(new ArrowFragment(this, arrow, event, start, dir));
				}
			}
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public double getRadius() {
		return radius;
	}

	public double getDamage() {
		return damage;
	}

	public double getAllyDamageReduction() {
		return allyDamageReduction;
	}

	public boolean isAllyCollide() {
		return allyCollide;
	}

	public Set<PotionType> getAllyExemptPotionTypes() {
		return allyExemptArrowTypes;
	}
}
