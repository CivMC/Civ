package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.InvisibleFixConfig;

/**
 * Sometimes when joining, a player is invisible due to some kind of spigot bug that's been
 * known since 2015 but unfixed.
 * 
 * This is widely reported as a fix -- hide then show the player to the other players online.
 * To do this safely w/o crashing all the things, I schedule the hide and show delayed
 * so if we get a huge influx of joins this doesn't take forever.
 *
 * @author ProgrammerDan
 */
public class InvisibleFix extends SimpleHack<InvisibleFixConfig> implements Listener {
	public static final String NAME = "InvisibleFix";
	private AtomicInteger activeJoins = new AtomicInteger(0);

	private LinkedTransferQueue<UUID> ltq = new LinkedTransferQueue<UUID>();
	private BukkitTask recheckTask = null;

	public InvisibleFix(SimpleAdminHacks plugin, InvisibleFixConfig config) {
		super(plugin, config);
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void JoinEvent(PlayerJoinEvent event) {
		if (!config.isEnabled()) return; // ignore if off
		Player p = event.getPlayer();
		if (p == null) return;
		final UUID uuid = p.getUniqueId();
		ltq.offer(uuid);

		if (p.isOp() && config.getIgnoreOps()) return;
		String tPerm = config.getIgnorePermission();
		if (tPerm != null && p.hasPermission(tPerm)) return;

		Bukkit.getScheduler().runTaskLater(plugin(), new Runnable() {
				@Override
				public void run() {
					plugin().debug("Hopefully fixing any invisibility for {0}", uuid);
					try {
						if (uuid == null) return;
						Player p = Bukkit.getPlayer(uuid);
						if (p == null) return;
						for (Player online : Bukkit.getOnlinePlayers()) {
							if (online != p) {
								online.hidePlayer(p);
								p.hidePlayer(online);
								online.showPlayer(p);
								p.showPlayer(online);
							}
						}
					} finally {
						if (activeJoins.decrementAndGet() < 0) {
							activeJoins.set(0);
						}
					}
				}
			}, activeJoins.incrementAndGet() * 20);
	}

	@Override
	public void registerListeners() {
		if (config.isEnabled()) {
			plugin().log("Registering InvisibleFix listener");
			plugin().registerListener(this);
			plugin().log("Starting recheck task");
			this.recheckTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin(), 
					new Runnable() {
						@Override
						public void run() {
							if (!config.isEnabled()) return;
							doRecheck();
						}				
					},
					config.getRecheckInterval(), config.getRecheckInterval());
		}
	}

	private void doRecheck() {
		// unspool, then recheck.
		plugin().debug("Refixing invisibles...");
		HashSet<Player> refix = new HashSet<Player>(config.getMaxPlayersPerRecheck());
		LinkedList<UUID> skiplist = new LinkedList<UUID>();
		int cnt = 0;
		// accumulate
		while (this.ltq.peek() != null && cnt < config.getMaxPlayersPerRecheck()) {
			UUID fixerUUID = ltq.poll();
			if (fixerUUID == null) break;
			Player fixer = Bukkit.getPlayer(fixerUUID);
			if (fixer != null) {
				if (fixer.isOp() && config.getIgnoreOps()) {
					skiplist.add(fixerUUID);
					continue;
				}
				String tPerm = config.getIgnorePermission();
				if (tPerm != null && fixer.hasPermission(tPerm)) {
					skiplist.add(fixerUUID);
					continue;
				}
				refix.add(fixer);
				cnt++;
			}
		}
		if (cnt > 0) {
			plugin().debug("Found {0} players to refix", cnt);
			// unblind
			for (Player online : Bukkit.getOnlinePlayers()) {
				for (Player fix : refix) {
					if (!refix.contains(fix)) {
						online.hidePlayer(fix);
						fix.hidePlayer(online);
						online.showPlayer(fix);
						fix.hidePlayer(online);
					}
				}
			}
			// add them back.
			for (Player fix : refix) {
				this.ltq.offer(fix.getUniqueId());
			}
		}
		for (UUID skip : skiplist) {
			this.ltq.offer(skip);
		}
	}

	@Override
	public void registerCommands() {
	}

	@Override
	public void dataBootstrap() {
		activeJoins.set(0);
		ltq.clear();
	}

	@Override
	public void unregisterListeners() {
		if (this.recheckTask != null) {
			plugin().debug("Stopping InvisibleFix recheck task");
			this.recheckTask.cancel();
		}
	}

	@Override
	public void unregisterCommands() {
	}

	@Override
	public void dataCleanup() {
		ltq.clear();
	}

	@Override
	public String status() {
		if (config != null && config.isEnabled()) {
			return "InvisibleFix active";
		} else {
			return "InvisibleFix not active";
		}
	}

	public static InvisibleFixConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new InvisibleFixConfig(plugin, config);
	}
}
