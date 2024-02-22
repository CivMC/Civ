package com.github.devotedmc.hiddenore;

import com.github.devotedmc.hiddenore.commands.CommandHandler;
import com.github.devotedmc.hiddenore.listeners.BlockBreakListener;
import com.github.devotedmc.hiddenore.listeners.ExploitListener;
import com.github.devotedmc.hiddenore.listeners.WorldGenerationListener;
import com.github.devotedmc.hiddenore.tracking.BreakTracking;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class HiddenOre extends JavaPlugin {

	private static HiddenOre plugin;

	private static CommandHandler commandHandler;

	private static BreakTracking tracking;
	private BukkitTask trackingSave;
	private BukkitTask trackingMapSave;
	
	private static BlockBreakListener breakHandler;
	private static ExploitListener exploitHandler;
	private static List<WorldGenerationListener> worldGen;

	@Override
	public void onEnable() {
		plugin = this;
		
		saveDefaultConfig();
		reloadConfig();
		Config.loadConfig();
		
		tracking = new BreakTracking();
		tracking.load();
		trackingSave = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
			public void run() {
				tracking.save();
			}
		}, Config.trackSave, Config.trackSave);

		trackingMapSave = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
			public void run() {
				tracking.saveMap();
			}
		}, Config.mapSave, Config.mapSave);

		
		exploitHandler = new ExploitListener(plugin);
		this.getServer().getPluginManager().registerEvents(exploitHandler, this);

		breakHandler = new BlockBreakListener(plugin);
		this.getServer().getPluginManager().registerEvents(breakHandler, this);
				
		commandHandler = new CommandHandler(this);
		this.getCommand("hiddenore").setExecutor(commandHandler);

		worldGen = new ArrayList<>();

		ConfigurationSection worldGenConfig = Config.instance.getWorldGenerations();
		if (worldGenConfig != null) {
			for (String key : worldGenConfig.getKeys(false)) {
				this.getLogger().log(Level.INFO, "Registered Ore Generation Suppression Listener for World {0}", key);
				WorldGenerationListener list = new WorldGenerationListener(worldGenConfig.getConfigurationSection(key));
				this.getServer().getPluginManager().registerEvents(list, this);
				worldGen.add(list);
			}
		}
	}

	@Override
	public void onDisable() {
		trackingSave.cancel();
		trackingMapSave.cancel();
		tracking.save();
		tracking.saveMap();
	}

	public static HiddenOre getPlugin() {
		return plugin;
	}

	public BreakTracking getTracking() {
		return tracking;
	}
	
	public BlockBreakListener getBreakListener() {
		return breakHandler;
	}
}
