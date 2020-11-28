package com.programmerdan.minecraft.simpleadminhacks.hacks.basic.AutoRespawn;

import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

final class _RespawnTimer {

	private final Consumer<Player> respawner;
	private BossBar bar;
	private long previousTime;
	private final long setTime;
	private long timeRemaining;
	private long secondTimer;
	private BukkitTask processor;

	_RespawnTimer(final AutoRespawn hack, final Player player, final long delay, final Consumer<Player> respawner) {
		this.respawner = respawner;
		this.previousTime = System.currentTimeMillis();
		this.setTime = this.timeRemaining = delay;
		this.secondTimer = 1000L;
		this.bar = Bukkit.createBossBar(generateBarTitle(), BarColor.WHITE, BarStyle.SOLID);
		this.bar.setVisible(true);
		this.bar.setProgress(1.0d);
		this.bar.addPlayer(player);
		this.processor = new BukkitRunnable() {
			@Override
			public void run() {
				tick();
			}
		}.runTaskTimer(hack.plugin(), 1L, 1L);
	}

	private String generateBarTitle() {
		if (this.timeRemaining <= 1000L) {
			return "Respawning now.";
		}
		if (this.timeRemaining < 60000L) {
			return "Respawning in " + (this.timeRemaining / 1000L) + " seconds.";
		}
		if (this.timeRemaining < 120000L) {
			return "Respawning in 1 minute.";
		}
		if (this.timeRemaining < 3600000L) {
			return "Respawning in " + (this.timeRemaining / 60000L) + " minutes.";
		}
		return "Respawning in " + (this.timeRemaining / 3600000L) + " hours.";
	}

	private void tick() {
		long currentTime = System.currentTimeMillis();
		long timeDifference = currentTime - this.previousTime;
		this.previousTime = currentTime;
		this.timeRemaining -= timeDifference;
		this.secondTimer -= timeDifference;
		if (this.secondTimer > 0) {
			return;
		}
		this.secondTimer = 1000L;
		this.bar.setTitle(generateBarTitle());
		this.bar.setProgress(Math.max((double) (this.timeRemaining - 1000L) / (double) this.setTime, 0));
		if (this.timeRemaining > 0) {
			return;
		}
		this.bar.getPlayers().forEach(this.respawner);
	}

	public _RespawnTimer stop() {
		if (this.bar != null) {
			this.bar.setVisible(false);
			this.bar.removeAll();
			this.bar = null;
		}
		if (this.processor != null) {
			this.processor.cancel();
			this.processor = null;
		}
		return null;
	}

}
