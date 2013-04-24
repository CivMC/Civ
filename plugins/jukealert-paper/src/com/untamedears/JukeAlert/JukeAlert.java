package com.untamedears.JukeAlert;

import com.untamedears.JukeAlert.command.CommandHandler;
import com.untamedears.JukeAlert.manager.ConfigManager;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.storage.JukeAlertLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class JukeAlert extends JavaPlugin {

	private static JukeAlert instance;
	private JukeAlertLogger jaLogger;
	private ConfigManager configManager;
	private List<Snitch> snitches = new ArrayList<>();

	@Override
	public void onEnable() {
		instance = this;
		jaLogger = new JukeAlertLogger();

		loadManagers();
		
		CommandHandler commands = new CommandHandler();
		for (String command : getDescription().getCommands().keySet()) {
			getCommand(command).setExecutor(commands);
		}
	}
	
	private void loadManagers()
	{
		configManager = new ConfigManager();
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
	
	public ConfigManager getConfigManager()
	{
		return configManager;
	}

	//Logs a message with the level of Info.
	public void log(String message) {
		this.getLogger().log(Level.INFO, message);
	}

	
}
