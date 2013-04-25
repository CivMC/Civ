package com.untamedears.JukeAlert.manager;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.storage.JukeAlertLogger;

public class SnitchManager {

	private JukeAlert plugin;
	private JukeAlertLogger logger;
	private Map<World, Map<Location, Snitch>> snitches;

	public SnitchManager() {
		plugin = JukeAlert.getInstance();
		logger = plugin.getJaLogger();
	}
	
	public void loadSnitches() {
		snitches = logger.getAllSnitches();
	}
	
	public void saveSnitches() {
		//TODO: saveSnitches
	}
	
	public Map<World, Map<Location, Snitch>> getSnitches() {
		return snitches;
	}
	
	public void setSnitches(Map<World, Map<Location, Snitch>> snitches)	{
		this.snitches = snitches;
	}
	
	public void addSnitch(Snitch snitch) {
		//TODO: addSnitch
	}
	
	public void removeSnitch(Snitch snitch)	{
		//TODO: removeSnitch
	}
}
