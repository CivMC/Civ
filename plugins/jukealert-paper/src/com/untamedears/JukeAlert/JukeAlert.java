package com.untamedears.JukeAlert;

import com.untamedears.JukeAlert.sql.Database;
import com.untamedears.JukeAlert.sql.JukeAlertLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class JukeAlert extends JavaPlugin {
	private JukeAlertSnitch jas;
	public static final Logger LOGGER = Logger.getLogger("Minecraft");
	private JukeAlertLogger jaLogger;
	static List<JukeAlertSnitch> snitches = new ArrayList<>(); //TODO: Add snitches to memory so it's not server intensive going to the SQL everytime.
	static List<JukeAlertSnitch> listOSnitches= new ArrayList<>().get(index);//index should be amount of snitches?
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
		jas.save();
		//TODO: Make sure everything saves properly and does save.
	}

	//Gets the JaLogger.
	public JukeAlertLogger getJaLogger() {
		return jaLogger;
	}

	//Logs a message with the level of Info.
	public static void log(String message) {
		log(Level.INFO, message);
	}

	//Logs a message with a level defined.
	public static void log(Level level, String message) {
		LOGGER.log(level, "[HCSMP] " + message);
	}

	//Logs a message with a level define and a throwable.
	public static void log(Level level, String message, Throwable thrown) {
		LOGGER.log(level, "[HCSMP] " + message, thrown);
	}
}
