package com.untamedears.JukeAlert;

import com.untamedears.JukeAlert.command.CommandHandler;
import com.untamedears.JukeAlert.listener.JukeAlertListener;
import com.untamedears.JukeAlert.manager.ConfigManager;
import com.untamedears.JukeAlert.manager.SnitchManager;
import com.untamedears.JukeAlert.storage.JukeAlertLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class JukeAlert extends JavaPlugin {

	private static JukeAlert instance;
	private JukeAlertLogger jaLogger;
	private ConfigManager configManager;
	private SnitchManager snitchManager;
        private Map<World, Map<Location, JukeAlertSnitch>> snitches = new HashMap<World, Map<Location, JukeAlertSnitch>>();

	@Override
	public void onEnable() {
		instance = this;
		jaLogger = new JukeAlertLogger();

		loadManagers();
		loadSnitches();
		registerEvents();
		
		CommandHandler commands = new CommandHandler();
		for (String command : getDescription().getCommands().keySet()) {
			getCommand(command).setExecutor(commands);
		}
	}
	
	private void loadManagers()
	{
		configManager = new ConfigManager();
		snitchManager = new SnitchManager();
	}
	
	public void loadSnitches() {
		snitchManager.loadSnitches();
	}
	
	public void registerEvents() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new JukeAlertListener(), this);
	}

	@Override
	public void onDisable() {
		//TODO: Make sure everything saves properly and does save.
	}
	
	public static JukeAlert getInstance() {
		return instance;
	}

	//Gets the JaLogger.
	public JukeAlertLogger getJaLogger() {
		return jaLogger;
	}
	
	public ConfigManager getConfigManager()	{
		return configManager;
	}
	
	public SnitchManager getSnitchManager() {
		return snitchManager;
	}

	public List<JukeAlertSnitch> getSnitches(World world) {
		return (List<JukeAlertSnitch>) snitches.get(world).values();
	}

	public JukeAlertSnitch getSnitch(World world, Location location) {
		return snitches.get(world).get(location);
	}

	//Logs a message with the level of Info.
	public void log(String message) {
		this.getLogger().log(Level.INFO, message);
	}
}
