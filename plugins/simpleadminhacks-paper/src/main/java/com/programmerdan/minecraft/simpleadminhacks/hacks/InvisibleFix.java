package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.InvisibleFixConfig;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.UUID;

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

	public InvisibleFix(SimpleAdminHacks plugin, InvisibleFixConfig config) {
		super(plugin, config);
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void JoinEvent(PlayerJoinEvent event) {
		if (!config.isEnabled()) return; // ignore if off
		Player p = event.getPlayer();
		if (p == null) return;
		final UUID uuid = p.getUniqueId();

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
								online.showPlayer(p);
							}
						}
					} finally {
						if (activeJoins.decrementAndGet() < 0) {
							activeJoins.set(0);
						}
					}
				}
			}, activeJoins.getAndIncrement());
	}

	@Override
	public void registerListeners() {
		if (config.isEnabled()) {
			plugin().log("Registering InvisibleFix listener");
			plugin().registerListener(this);
		}
	}

	@Override
	public void registerCommands() {
	}

	@Override
	public void dataBootstrap() {
		activeJoins.set(0);
	}

	@Override
	public void unregisterListeners() {
	}

	@Override
	public void unregisterCommands() {
	}

	@Override
	public void dataCleanup() {
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
