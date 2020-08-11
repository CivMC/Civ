package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.PhantomMenaceConfig;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;

public class PhantomMenace extends SimpleHack<PhantomMenaceConfig> implements Listener {

	public PhantomMenace(SimpleAdminHacks plugin, PhantomMenaceConfig config) {
		super(plugin, config);
	}

	@Override
	public String status() {
		if (!config.isEnabled()) {
			return "Phantom Manager disabled.";
		}
		return String.format("Phantom Manager enabled. [CAP: %S, NIGHT: %S, STORM: %S]",
				config.getTimeSinceRestCap(), config.canNightSpawn(), config.canStormSpawn());
	}

	// ------------------------------------------------------------
	// Listeners
	// ------------------------------------------------------------

	@Override
	public void registerListeners() {
		this.plugin().registerListener(this);
	}

	@Override
	public void unregisterListeners() {
		HandlerList.unregisterAll(this);
	}

	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		// Do not regulate unless the creature is a phantom
		if (event.getEntityType() != EntityType.PHANTOM) {
			return;
		}
		// Do not regulate phantom spawning unless it's natural
		switch (event.getSpawnReason()) {
			case NATURAL:
			case DEFAULT:
				break;
			default:
				return;
		}
		if (!config.canNightSpawn() && !config.canStormSpawn()) {
			event.setCancelled(true);
			plugin().debug("Phantom prevented from spawning: cannot spawn during the night nor during storms.");
			return;
		}
		World world = event.getLocation().getWorld();
		if (world == null) {
			return;
		}
		if (!config.canNightSpawn() && config.canStormSpawn()) {
			if (!world.hasStorm()) {
				event.setCancelled(true);
				plugin().debug("Phantom prevented from spawning: night spawning disabled.");
			}
		}
		else if (config.canNightSpawn() && !config.canStormSpawn()) {
			if (world.getTime() < 12300 || world.getTime() > 23850) {
				event.setCancelled(true);
				plugin().debug("Phantom prevented from spawning: storm spawning disabled.");
			}
		}
	}

	@EventHandler
	public void onStatisticIncrease(PlayerStatisticIncrementEvent event) {
		if (event.getStatistic() != Statistic.TIME_SINCE_REST) {
			return;
		}
		if (event.getPreviousValue() < 72_000 && event.getNewValue() >= 72_000) {
			event.getPlayer().sendMessage(ChatColor.DARK_RED + "You haven't slept in a while...");
		}
		int cap = config.getTimeSinceRestCap();
		if (cap <= -1 || event.getNewValue() < cap) {
			return;
		}
		event.getPlayer().setStatistic(Statistic.TIME_SINCE_REST,
				Math.min(event.getNewValue(), cap));
		event.setCancelled(true); // Prevent the increment
	}

	// ------------------------------------------------------------
	// Commands
	// ------------------------------------------------------------

	@Override
	public void registerCommands() { }

	@Override
	public void unregisterCommands() { }

	// ------------------------------------------------------------
	// Setup
	// ------------------------------------------------------------

	@Override
	public void dataBootstrap() { }

	@Override
	public void dataCleanup() { }

	public static PhantomMenaceConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new PhantomMenaceConfig(plugin, config);
	}

}
