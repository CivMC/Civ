package com.untamedears.JukeAlert;

import com.untamedears.JukeAlert.sql.JukeAlertLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class JukeAlert extends JavaPlugin {

	private JukeAlertLogger jaLogger;
	private List<JukeAlertSnitch> snitches = new ArrayList<>();

	@Override
	public void onEnable() {
		jaLogger = new JukeAlertLogger(this);

		JukeAlertCommands commands = new JukeAlertCommands(this);
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
