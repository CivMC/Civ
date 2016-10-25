package com.devotedmc.ExilePearl.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import com.devotedmc.ExilePearl.DamageLogger;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.config.PearlConfig;
import com.devotedmc.ExilePearl.event.PlayerPearledEvent;

import vg.civcraft.mc.civmodcore.util.Guard;

/**
 * This class tracks damage dealt between players for the purpose
 * of decided who should be awarded the pearl of a killed player.
 * 
 * @author Gordon
 */
final class CoreDamageLogger extends ExilePearlTask implements DamageLogger {

	private final Map<UUID, DamageLog> damageLogs = new HashMap<UUID, DamageLog>();
	
	private List<PotionEffectType> damagePotions = Arrays.asList(PotionEffectType.HARM, PotionEffectType.POISON, PotionEffectType.WEAKNESS);

	// The bits for indicating whether a given potion is upgraded or extended
	private static final short POTION_UPGRADE_MASK = 1 << 5;
	private static final short POTION_EXTENDED_MASK = POTION_UPGRADE_MASK << 1;
	private static final short POTION_MULTIPLIER_MASK = POTION_EXTENDED_MASK | POTION_UPGRADE_MASK;

	private int interval = 20;
	private int algorithm = 0;
	private double decayAmount = 1.0;
	private double maxDamage = 30.0;
	private double potionDamage = 6.0;

	/**
	 * Creates a new CoreDamageLogger instance
	 * @param pearlApi The pearl API
	 */
	public CoreDamageLogger(ExilePearlApi pearlApi) {
		super(pearlApi);
	}

	@Override
	public void start() {

		super.start();
		if (enabled) {
			pearlApi.log("Damage logger will run every %d ticks.", interval);
		}
	}

	@Override
	public void run() {
		Iterator<DamageLog> it = damageLogs.values().iterator();
		while (it.hasNext()) {
			DamageLog log = it.next();
			if (!log.decayDamage(decayAmount)) {
				it.remove();
			}
		}
	}

	@Override
	public String getTaskName() {
		return "Damage Logger";
	}

	@Override
	public int getTickInterval() {
		return interval;
	}

	@Override
	public void recordDamage(Player player, Player damager, double amount) {
		Guard.ArgumentNotNull(player, "player");
		Guard.ArgumentNotNull(damager, "damager");
		
		DamageLog rec = damageLogs.get(player.getUniqueId());
		if (rec == null) {
			rec = new DamageLog(pearlApi.getClock(), player.getUniqueId());
			damageLogs.put(player.getUniqueId(), rec);
		}

		rec.recordDamage(damager, amount, maxDamage);
	}

	@Override
	public List<Player> getSortedDamagers(Player player) {
		Guard.ArgumentNotNull(player, "player");
		
		final List<Player> players = new ArrayList<Player>();
		final DamageLog log = damageLogs.get(player.getUniqueId());
		
		if (log == null) {
			return players;
		}
		
		// Algorithm 0 sorts by most recent
		// Algorithm 1 sorts by greatest damage
		Collection<DamageRecord> recs = null;
		if (algorithm == 0) {
			recs = log.getTimeSortedDamagers();
		} else {
			recs = log.getDamageSortedDamagers();
		}
		
		for (DamageRecord rec : recs) {
			Player p = Bukkit.getPlayer(rec.getDamager());
			if (p != null && p.isOnline()) {
				players.add(p);
			}
		}
		
		return players;
	}
	
	

	@Override
	public void loadConfig(PearlConfig config) {
		super.loadConfig(config);

		int newInterval = config.getDamageLogInterval();
		algorithm = config.getDamageLogAlgorithm();
		decayAmount = config.getDamageLogDecayAmount();
		maxDamage = config.getDamageLogMaxDamage();
		potionDamage = config.getDamageLogPotionDamage();
		
		if (algorithm < 0 || algorithm > 1) {
			algorithm = 0;
		}

		if (newInterval != interval) {
			this.interval = newInterval;

			// Reschedule the task if the interval changed
			if (enabled) {
				pearlApi.log("Rescheduling the damage log task because the interval changed.");
				restart();
			}
		}
	}

	/**
	 * Remove tracking for players who die
	 * @param e The event args
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent e) {
		if (!(e.getEntity() instanceof Player)) {
			return;
		}
		Player p = (Player)e.getEntity();

		damageLogs.remove(p.getUniqueId());
	}


	/**
	 * Remove tracking for players who quit
	 * @param event The event args
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent e) {
		damageLogs.remove(e.getPlayer().getUniqueId());
	}


	/**
	 * Remove tracking for players who are pearled
	 * @param e The event args
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerPearled(PlayerPearledEvent e) {
		damageLogs.remove(e.getPearl().getPlayerId());
	}


	/**
	 * Record damage dealt to players
	 * @param e The event args
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof Player)) {
			return;
		}

		Player player = (Player) e.getEntity();

		Player damager = null;
		if (e.getDamager() instanceof Player) {
			damager = (Player) e.getDamager();
		} else if (e.getDamager() instanceof Wolf) {
			Wolf wolf = (Wolf) e.getDamager();
			if (wolf.getOwner() instanceof Player) {
				damager = (Player) wolf.getOwner();
			}
		} else if (e.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) e.getDamager();
			if (!(arrow.getShooter() instanceof Player)) {
				return;
			}
			damager = (Player) arrow.getShooter();
		} else if (e.getDamager() instanceof Snowball) {
			Snowball snowball = (Snowball)e.getDamager();
			if (!(snowball.getShooter() instanceof Player)) {
				return;
			}
			damager = (Player) snowball.getShooter();
		}

		if (damager == null || damager == player) {
			return;
		}

		recordDamage(player, damager, e.getDamage());
	}

	/**
	 * Tracks damage from potions
	 * @param e The event args
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPotionSplashEvent(PotionSplashEvent e) {
		ProjectileSource ps = e.getPotion().getShooter();
		LivingEntity shooter;
		if (!(ps instanceof LivingEntity)) {
			return;
		}
		shooter = (LivingEntity) ps;
		if (!(shooter instanceof Player)) {
			return;
		}
		
		
		Player damager = (Player) shooter;

		boolean isDamagePotion = false;
		for (PotionEffect effect : e.getPotion().getEffects()) {
			if (damagePotions.contains(effect.getType())) {
				isDamagePotion = true;
				break;
			}
		}

		// No valid effect found to log
		if (!isDamagePotion) {
			return;
		}
		
		// If a potion is upgraded or extended it will deal twice the base damage
		double damage = potionDamage;
		if ((e.getEntity().getItem().getDurability() & POTION_MULTIPLIER_MASK) > 0) {
			damage *= 2;
		}

		// Deal damage to all affected players
		for (LivingEntity entity : e.getAffectedEntities()) {
			if (!(entity instanceof Player)) {
				continue;
			}

			recordDamage((Player) entity, damager, damage * e.getIntensity(entity));
		}
	}
}
