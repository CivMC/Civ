package com.github.devotedmc.hiddenore;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.github.devotedmc.hiddenore.commands.CommandHandler;
import com.github.devotedmc.hiddenore.listeners.BlockBreakListener;
import com.github.devotedmc.hiddenore.tracking.BreakTracking;

public class HiddenOre extends JavaPlugin {

	private static HiddenOre plugin;

	private static CommandHandler cm;

	private static BreakTracking bt;

	private BukkitTask btSave;

	@Override
	public void onEnable() {
		plugin = this;
		saveDefaultConfig();
		reloadConfig();
		Config.loadConfig();
		bt = new BreakTracking();
		bt.load();
		btSave = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
			public void run() {
				bt.save();
			}
		}, Config.trackSave, Config.trackSave);
		Bukkit.getPluginManager().registerEvents(new BlockBreakListener(), this);
		cm = new CommandHandler(this);
	}

	@Override
	public void onDisable() {
		bt.save();
		btSave.cancel();
	}

	public static HiddenOre getPlugin() {
		return plugin;
	}

	public static BreakTracking getTracking() {
		return bt;
	}
}
