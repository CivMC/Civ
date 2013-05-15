package com.untamedears.JukeAlert.model;

import com.untamedears.citadel.entity.Faction;
import com.untamedears.JukeAlert.util.QTBox;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public class Snitch implements QTBox, Comparable {

    private int snitchId;
    private String name;
    private Location location;
    private Faction group;
    private int minx, maxx, miny, maxy, minz, maxz, radius;

    public Snitch(Location loc, Faction group) {
        this.group = group;
        this.location = loc;
        this.name = "";
        radius = 11; 
        calculateDimensions();
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

    // interface QTBox
    public int qtX1() {
        return this.minx;
    }

    public int qtX2() {
        return this.maxx;
    }

    public int qtY1() {
        return this.minz;
    }

    public int qtY2() {
        return this.maxz;
    }
    // end interface QTBox

    // interface Comparable
    @Override
    public int compareTo(Object o) {
      Snitch other = (Snitch)o;
      if (this.minx == other.minx
          && this.maxx == other.maxx
          && this.miny == other.miny
          && this.maxy == other.maxy
          && this.minz == other.minz
          && this.maxz == other.maxz) {
        return 0;  // Equal
      }
      if (this.minx >= other.minx
          && this.maxx <= other.maxx
          && this.miny >= other.miny
          && this.maxy <= other.maxy
          && this.minz >= other.minz
          && this.maxz <= other.maxz) {
        // This is contained within other
        return -1;  // Less
      }
      if (this.minx <= other.minx
          && this.maxx >= other.maxx
          && this.miny <= other.miny
          && this.maxy >= other.maxy
          && this.minz <= other.minz
          && this.maxz >= other.maxz) {
        // Other is contained within this
        return 1;  // Greater
      }
      if (this.minx < other.minx
          || this.miny < other.miny
          || this.minz < other.minz) {
        return -1;
      }
      return 1;
    }
    // end interface Comparable

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
    
    public String getName() {
        return this.name;
    }
    
    public void setId(int newId) {
        this.snitchId = newId;
    }
    
    public void setName(String name) {
        this.name = name;
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
        calculateDimensions();
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
        if (vX >= minx && vX <= maxx && vY >= miny && vY <= maxy && vZ >= minz && vZ <= maxz) {
            return true;
        }

        return false;
    }

    public boolean isWithinHeight(int y) {
        return y >= miny && y <= maxy;
    }

    public boolean at(Location loc) {
        return
            this.location.getBlockX() == loc.getBlockX()
	    && this.location.getBlockY() == loc.getBlockY()
	    && this.location.getBlockZ() == loc.getBlockZ();
    }
}
