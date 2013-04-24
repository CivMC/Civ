package com.untamedears.JukeAlert;

import com.untamedears.JukeAlert.command.CommandHandler;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.storage.JukeAlertLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class JukeAlert extends JavaPlugin {

	private JukeAlertLogger jaLogger;
	private List<Snitch> snitches = new ArrayList<>();

	@Override
	public void onEnable() {
		jaLogger = new JukeAlertLogger(this);

		CommandHandler commands = new CommandHandler(this);
		for (String command : getDescription().getCommands().keySet()) {

			getCommand(command).setExecutor(commands);
		}
	}

	@Override
	public void onDisable() {
		//TODO: Make sure everything saves properly and does save.
	}

	//Gets the JaLogger.
	public JukeAlertLogger getJaLogger() {
		return jaLogger;
	}

	//Logs a message with the level of Info.
	public void log(String message) {
		this.getLogger().log(Level.INFO, message);
	}

	
}
