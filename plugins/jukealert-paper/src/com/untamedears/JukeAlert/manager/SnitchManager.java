package com.untamedears.JukeAlert.manager;

import java.util.ArrayList;
import java.util.List;

import com.untamedears.JukeAlert.model.Snitch;

public class SnitchManager {

	private List<Snitch> snitches;
	
	public SnitchManager() {
		snitches = new ArrayList<Snitch>();
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
