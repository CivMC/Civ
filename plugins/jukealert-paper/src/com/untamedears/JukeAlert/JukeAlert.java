package com.untamedears.JukeAlert;

import com.untamedears.JukeAlert.command.Command;
import com.untamedears.JukeAlert.command.CommandHandler;
import com.untamedears.JukeAlert.command.commands.HelpCommand;
import com.untamedears.JukeAlert.command.commands.InfoCommand;
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
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class JukeAlert extends JavaPlugin {

	private static JukeAlert instance;
	private JukeAlertLogger jaLogger;
	private ConfigManager configManager;
	private SnitchManager snitchManager;
	private CommandHandler commandHandler;
        
	@Override
	public void onEnable() {
		instance = this;
		jaLogger = new JukeAlertLogger();

		loadManagers();
		loadSnitches();
		registerEvents();
		registerCommands();
	}

	@Override
	public void onDisable() {
		saveSnitches();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return commandHandler.dispatch(sender, label, args);
	}
	
	private void loadManagers() {
		configManager = new ConfigManager();
		snitchManager = new SnitchManager();
	}
	
	private void loadSnitches() {
		snitchManager.loadSnitches();
	}
	
	private void saveSnitches() {
		snitchManager.saveSnitches();
	}
	
	private void registerEvents() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new JukeAlertListener(), this);
	}
	
	private void registerCommands() {
		commandHandler = new CommandHandler();
		commandHandler.addCommand(new InfoCommand());
		commandHandler.addCommand(new HelpCommand());
	}
	
	public static JukeAlert getInstance() {
		return instance;
	}

	public JukeAlertLogger getJaLogger() {
		return jaLogger;
	}
	
	public ConfigManager getConfigManager()	{
		return configManager;
	}
	
	public SnitchManager getSnitchManager() {
		return snitchManager;
	}
	
	public CommandHandler getCommandHandler() {
		return commandHandler;
	}

	//Logs a message with the level of Info.
	public void log(String message) {
		this.getLogger().log(Level.INFO, message);
	}
}
