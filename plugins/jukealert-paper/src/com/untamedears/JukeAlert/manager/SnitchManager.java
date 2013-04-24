package com.untamedears.JukeAlert.manager;

import java.util.List;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.storage.JukeAlertLogger;

public class SnitchManager {

	private JukeAlert plugin;
	private List<Snitch> snitches;
	private JukeAlertLogger logger;
	
	public SnitchManager() {
		plugin = JukeAlert.getInstance();
		logger = plugin.getJaLogger();
	}
	
	public void loadSnitches() {
		snitches = logger.getAllSnitches();
	}
	
	public List<Snitch> getSnitches()
	{
		return snitches;
	}
	
	public void setSnitches(List<Snitch> snitches)
	{
		this.snitches = snitches;
	}
	
	public void addSnitch(Snitch snitch)
	{
		snitches.add(snitch);
	}
	
	public void removeSnitch(Snitch snitch)
	{
		snitches.remove(snitch);
	}
}
