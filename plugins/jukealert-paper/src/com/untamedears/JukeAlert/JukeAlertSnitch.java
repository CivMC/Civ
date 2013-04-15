package com.untamedears.JukeAlert;

import com.untamedears.citadel.entity.Faction;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;

public class JukeAlertSnitch {

	private Location loc;
	private Faction group;
	private int x, y, z;
	private List<String> inProximity = new ArrayList<>(); //Contains all the players who are within the proximity of the snitch.

	public JukeAlertSnitch(Location loc, Faction group) {
		this.group = group;
		this.loc = loc;
	}

	public Faction getGroup() {
		return group;
	}

	public void setGroup(Faction group) {
		this.group = group;
	}

	public Location getLoc() {
		return loc;
	}

	public void setLoc(Location loc) {
		this.loc = loc;
	}

	//Removes a player from the proximity.
	public boolean remove(String o) {
		return inProximity.remove(o);
	}

	//Adds a player to the proximity.
	public boolean add(String e) {
		return inProximity.add(e);
	}

	public void save() {
		//TODO: Make it update the SQL Database onDisable(), and or on a SaveTask.
	}

	public void load() {
		//TODO: Make it load from the SQL Database when the server starts up.
	}
}
