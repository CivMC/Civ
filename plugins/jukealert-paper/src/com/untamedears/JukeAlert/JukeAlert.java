package com.untamedears.JukeAlert;


import org.bukkit.plugin.java.JavaPlugin;



public class JukeAlert extends JavaPlugin{
	
	public void onEnable(){
		
	
		JukeAlertCommands commands = new JukeAlertCommands(this);
		for (String command : getDescription().getCommands().keySet()) {
			
			
			getCommand(command).setExecutor(commands);
		}

	}

	public void onDisable(){
		
	}
}

