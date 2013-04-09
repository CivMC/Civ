package com.untamedears.JukeAlert;

public class JukeAlert {
	
	public void onEnable(){
		JukeAlertCommands command= new JukeAlertCommands(this);
		
		getcommand(command).setExecutor(new JukeAlertCommands(this));
		
	}

	public void onDisable(){
		
	}
}

