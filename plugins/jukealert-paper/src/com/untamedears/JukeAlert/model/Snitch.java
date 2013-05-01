package com.untamedears.JukeAlert.model;

import com.untamedears.citadel.entity.Faction;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public class Snitch {

    private int snitchId;
    private Location location;
    private Faction group;
    private int x, y, z, minx, maxx, miny, maxy, minz, maxz, radius;
    private List<String> inProximity = new ArrayList<>(); //Contains all the players who are within the proximity of the snitch.

    public Snitch(Location loc, Faction group) {
        this.group = group;
        this.location = loc;
        radius = 11;
        
        
    }

    public int getX() {
        return location.getBlockX();
    }

    public int getY() {
        return location.getBlockY();
    }

    public int getZ() {
        return location.getBlockZ();
    }

    public void calculateDimensions() {
        this.minx = getX() - radius;
        this.maxx = getX() + radius;
        this.minz = getZ() - radius;
        this.maxz = getZ() + radius;
        this.miny = getY() - radius;
        this.maxy = getY() + radius;
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
        return location;
    }

    public void setLoc(Location loc) {
        this.location = loc;
    }

    //Removes a player from the proximity.
    public boolean remove(String o) {
        return inProximity.remove(o);
    }

    //Adds a player to the proximity.
    public boolean add(String e) {
        return inProximity.add(e);
    }
    
    public boolean checkProximity(String e) {
    	return inProximity.contains(e);
    }

    //Checks if the location is within the cuboid.
    public boolean isWithinCuboid(Location loc) {
        return isWithinCuboid(new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    }
    
    //Checks if the block is within the cuboid.
    public boolean isWithinCuboid(Block block) {
        return isWithinCuboid(new Vector(block.getX(), block.getY(), block.getZ()));
    }

    //Checks if the vector is within the cuboid.
    public boolean isWithinCuboid(Vector vec) {
        int vX = vec.getBlockX();
        int vY = vec.getBlockY();
        int vZ = vec.getBlockZ();
        calculateDimensions();
        if (vX >= minx && vX <= maxx && vY >= miny && vY <= maxy && vZ >= minz && vZ <= maxz) {
            return true;
        }

        return false;
    }
}
