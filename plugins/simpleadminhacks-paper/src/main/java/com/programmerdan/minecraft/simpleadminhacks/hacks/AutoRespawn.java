package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.configs.AutoRespawnConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHack;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import vg.civcraft.mc.civmodcore.util.TextUtil;

public class AutoRespawn extends SimpleHack<AutoRespawnConfig> implements Listener {
	
	private final Random random = new SecureRandom();
	private final Map<Player, RespawnTimer> respawnTimers = new HashMap<>();
	
	public AutoRespawn(SimpleAdminHacks plugin, AutoRespawnConfig config) {
		super(plugin, config);
	}
	
	private void autoRespawnPlayer(Player player) {
		player.spigot().respawn();
		String[] quotes = config.getRespawnQuotes();
		if (!ArrayUtils.isEmpty(quotes)) {
			player.sendMessage(TextUtil.parse(quotes[quotes.length == 1 ? 0 : random.nextInt(quotes.length)]));
		}
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerJoinEvent event) {
		if (event.getPlayer().isDead()) {
			plugin().info("Player [" + event.getPlayer().getName() + "] logged in while dead, respawning.");
			if (config.getLoginRespawnDelay() == 0) {
				autoRespawnPlayer(event.getPlayer());
			}
			else {
				this.respawnTimers.compute(event.getPlayer(), (player, timer) -> {
					if (timer != null) {
						timer.stop();
					}
					return new RespawnTimer(player, config.getLoginRespawnDelay());
				});
			}
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		plugin().info("Player [" + event.getEntity().getName() + "] died, setting respawn timer.");
		this.respawnTimers.compute(event.getEntity(), (player, timer) -> {
			if (timer != null) {
				timer.stop();
			}
			return new RespawnTimer(player, config.getRespawnDelay());
		});
	}
	
	@EventHandler
	public void onPlayerLogout(PlayerQuitEvent event) {
		if (event.getPlayer().isDead()) {
			plugin().info("Player [" + event.getPlayer().getName() + "] logged out while dead.");
			this.respawnTimers.computeIfPresent(event.getPlayer(), (player, timer) -> timer.stop());
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		this.respawnTimers.computeIfPresent(event.getPlayer(), (player, timer) -> timer.stop());
	}
	
	@Override
	public void dataCleanup() {
		this.respawnTimers.forEach((player, timer) -> timer.stop());
		this.respawnTimers.clear();
	}
	
	@Override
	public void registerListeners() {
		if (config.isEnabled()) {
			plugin().log("Registering AutoRespawn listener");
			plugin().registerListener(this);
		}
	}
	
	@Override
	public void unregisterListeners() {
		HandlerList.unregisterAll(this);
	}
	
	@Override
	public String status() {
		if (!config.isEnabled()) {
			return "Auto Respawner disabled.";
		}
		return "Auto Respawner enabled (" + this.respawnTimers.size() + " players are waiting to respawn)";
	}
	
	@Override
	public void dataBootstrap() {}
	
	@Override
	public void registerCommands() {}
	
	@Override
	public void unregisterCommands() {}
	
	public class RespawnTimer {
		
		private BossBar bar;
		private long previousTime;
		private final long setTime;
		private long timeRemaining;
		private long secondTimer;
		private BukkitTask processor;
		
		public RespawnTimer(Player player, long delay) {
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
			}.runTaskTimer(plugin(), 1L, 1L);
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
			this.bar.getPlayers().forEach(AutoRespawn.this::autoRespawnPlayer);
		}
		
		public RespawnTimer stop() {
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
	
	public static AutoRespawnConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new AutoRespawnConfig(plugin, config);
	}
	
}
