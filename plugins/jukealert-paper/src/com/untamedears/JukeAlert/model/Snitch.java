package com.untamedears.JukeAlert.model;

import com.untamedears.citadel.entity.Faction;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Snitch {

	private int snitchId;
    private Location loc;
    private Faction group;
    private int x, y, z;
    private List<String> inProximity = new ArrayList<>(); //Contains all the players who are within the proximity of the snitch.

    public Snitch(Location loc, Faction group) {
        this.group = group;
        this.loc = loc;
    }
    
    public int getId() {
    	return this.snitchId;
    }
    
    public void setId(int newId) {
    	this.snitchId = newId;
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

    //Checks if the location is within the cuboid.
    public boolean isWithinCuboid(Location loc) {
        //TODO: Check if the location is within the Cuboid of int x, y, z.
        return false;
    }


}
