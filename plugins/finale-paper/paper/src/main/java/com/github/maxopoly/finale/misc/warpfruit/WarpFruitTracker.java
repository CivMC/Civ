package com.github.maxopoly.finale.misc.warpfruit;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.external.FinaleSettingManager;
import com.github.maxopoly.finale.misc.CooldownHandler;
import com.github.maxopoly.finale.misc.ally.AllyHandler;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLine;
import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLineAPI;
import vg.civcraft.mc.civmodcore.players.scoreboard.side.CivScoreBoard;
import vg.civcraft.mc.civmodcore.players.scoreboard.side.ScoreBoardAPI;
import vg.civcraft.mc.civmodcore.utilities.cooldowns.ICoolDownHandler;
import vg.civcraft.mc.civmodcore.utilities.cooldowns.TickCoolDownHandler;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.BiFunction;

public class WarpFruitTracker {

	private static final double ANGLE_INCREMENT = 10;
	private static final double RADIUS = 1;

	private int logSize;
	private long logInterval;
	private long warpFruitCooldown;
	private double maxDistance;
	private boolean spectralWhileChanneling;

	private List<PotionEffect> afterEffects;

	private CooldownHandler cooldownHandler;
	private Map<UUID, WarpFruitData> warpFruitDataMap = new HashMap<>();

	public WarpFruitTracker(int logSize, long logInterval, long warpFruitCooldown, double maxDistance, boolean spectralWhileChanneling, List<PotionEffect> afterEffects) {
		this.logSize = logSize;
		this.logInterval = logInterval;
		this.warpFruitCooldown = warpFruitCooldown;
		this.maxDistance = maxDistance;
		this.spectralWhileChanneling = spectralWhileChanneling;
		this.afterEffects = afterEffects;

		this.cooldownHandler = new CooldownHandler("warpfruitCooldown", warpFruitCooldown, (player, cooldowns) ->
				ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Warp fruit: " +
				ChatColor.AQUA + CooldownHandler.formatCoolDown(cooldowns, player.getUniqueId())
		);
	}

	public boolean isSpectralWhileChanneling() {
		return spectralWhileChanneling;
	}

	public WarpFruitData getWarpFruitData(Player player) {
		WarpFruitData warpFruitData = warpFruitDataMap.get(player.getUniqueId());
		if (warpFruitData == null) {
			warpFruitData = new WarpFruitData(this);
			warpFruitDataMap.put(player.getUniqueId(), warpFruitData);
		}
		return warpFruitData;
	}

	public void animate(Player player) {
		WarpFruitData warpFruitData = getWarpFruitData(player);
		double animateAngle = warpFruitData.getAnimateAngle();

		Location playerLoc = player.getLocation();
		double px = playerLoc.getX();
		double py = playerLoc.getY() + 0.2;
		double pz = playerLoc.getZ();

		double dx = RADIUS * Math.sin(Math.toRadians(animateAngle));
		double dz = RADIUS * Math.cos(Math.toRadians(animateAngle));

		Location timeWarpLocation = warpFruitData.getTimeWarpLocation();
		double tx = timeWarpLocation.getX();
		double ty = timeWarpLocation.getY() + 0.2;
		double tz = timeWarpLocation.getZ();

		World world = player.getWorld();
		Location playerParticleLoc = new Location(world, px + dx, py, pz + dz);
		Location targetParticleLoc = new Location(world, tx + dx, ty, tz + dz);

		ParticleAudience audience = getParticleAudience(player);

		spawnParticles(audience, playerParticleLoc);
		spawnParticles(audience, targetParticleLoc);

		animateAngle += ANGLE_INCREMENT;
		warpFruitData.setAnimateAngle(animateAngle);
	}

	private void spawnParticles(ParticleAudience audience, Location loc) {
		List<Player> self = audience.getSelf();
		List<Player> allies = audience.getAllies();
		List<Player> rest = audience.getRest();

		Particle.DustOptions dustOptions = new Particle.DustOptions(Color.PURPLE, 1);
		loc.getWorld().spawnParticle(Particle.REDSTONE, rest, null, loc.getX(), loc.getY(), loc.getZ(),
				1, 0, 0, 0, 0, dustOptions, true);

		dustOptions = new Particle.DustOptions(Color.BLUE, 1);
		loc.getWorld().spawnParticle(Particle.REDSTONE, allies, null, loc.getX(), loc.getY(), loc.getZ(),
				1, 0, 0, 0, 0, dustOptions, true);

		dustOptions = new Particle.DustOptions(Color.GREEN, 1);
		loc.getWorld().spawnParticle(Particle.REDSTONE, self, null, loc.getX(), loc.getY(), loc.getZ(),
				1, 0, 0, 0, 0, dustOptions, true);
	}

	public void logLocation(Player player) {
		WarpFruitData warpFruitData = getWarpFruitData(player);
		warpFruitData.logLocation(player.getLocation());
	}

	private void ring(Player player, Location location) {
		ParticleAudience audience = getParticleAudience(player);

		for (double angle = 0; angle < 360; angle += ANGLE_INCREMENT) {
			Location loc = location.clone();
			double dx = RADIUS * Math.sin(Math.toRadians(angle));
			double dz = RADIUS * Math.cos(Math.toRadians(angle));

			Location partLoc = loc.add(dx, 0.3, dz);
			spawnParticles(audience, partLoc);
		}
	}

	public boolean timewarp(Player player) {
		if (onCooldown(player)) {
			return false;
		}

		WarpFruitData warpFruitData = warpFruitDataMap.get(player.getUniqueId());
		if (warpFruitData == null) {
			return false;
		}

		Location warpLoc = warpFruitData.getTimeWarpLocation();
		if (warpLoc == null) {
			return false;
		}

		ring(player, player.getLocation());
		ring(player, warpLoc);

		World world = warpLoc.getWorld();
		world.playSound(warpLoc, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1f, 1f);
		player.setFallDistance(0);
		player.teleport(warpLoc);
		if (!afterEffects.isEmpty()) {
			player.addPotionEffects(afterEffects);
		}
		putOnCooldown(player);
		return true;
	}

	public void putOnCooldown(Player shooter) {
		cooldownHandler.putOnCooldown(shooter);
	}

	public void quit(Player player) {
		cooldownHandler.quit(player);
		warpFruitDataMap.remove(player.getUniqueId());
	}

	public boolean onCooldown(Player player) {
		return cooldownHandler.onCooldown(player);
	}

	public int getLogSize() {
		return logSize;
	}

	public long getLogInterval() {
		return logInterval;
	}

	public long getWarpFruitCooldown() {
		return warpFruitCooldown;
	}

	private ParticleAudience getParticleAudience(Player player) {
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		List<Player> self = Arrays.asList(player);

		AllyHandler allyHandler = Finale.getPlugin().getManager().getAllyHandler();
		Set<UUID> allyIDs = allyHandler.getAllies(player);
		List<Player> allies = new ArrayList<>();
		List<Player> rest = new ArrayList<>();

		for (Player onlinePlayer : players) {
			if (allyIDs.contains(onlinePlayer.getUniqueId())) {
				allies.add(onlinePlayer);
			} else if (!onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
				rest.add(onlinePlayer);
			}
		}

		ParticleAudience result = new ParticleAudience();
		result.setSelf(self);
		result.setAllies(allies);
		result.setRest(rest);
		return result;
	}

	private static class ParticleAudience {

		private List<Player> self;
		private List<Player> allies;
		private List<Player> rest;

		public List<Player> getSelf() {
			return self;
		}

		public void setSelf(List<Player> self) {
			this.self = self;
		}

		public List<Player> getAllies() {
			return allies;
		}

		public void setAllies(List<Player> allies) {
			this.allies = allies;
		}

		public List<Player> getRest() {
			return rest;
		}

		public void setRest(List<Player> rest) {
			this.rest = rest;
		}
	}
}
