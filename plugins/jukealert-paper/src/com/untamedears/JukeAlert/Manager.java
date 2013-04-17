package com.untamedears.JukeAlert;

public class Manager {
	private JukeAlertSnitch jas;
    public void save() {
        //TODO: Make it update the SQL Database onDisable(), and or on a SaveTask.
    }

    public void load() {
    	JukeAlert.snitches.add(jas);
    	
    	
        //TODO: Make it load from the SQL Database when the server starts up.
        /*
         * JukeAlert.snitches.add(this);
         */
    }

}
