package com.untamedears.JukeAlert.model;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.namelayer.group.Group;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.manager.ConfigManager;
import com.untamedears.JukeAlert.util.QTBox;

public class Snitch implements QTBox, Comparable {

    private int snitchId;
    private String name;
    private Location location;
    private Group group;
    private boolean shouldLog;
    private boolean shouldToggleLevers;
    private int minx, maxx, miny, maxy, minz, maxz, radius;

    public Snitch(Location loc, Group group, boolean shouldLog, boolean shouldToggleLevers) {
        this.group = group;
        this.shouldLog = shouldLog;
        this.location = loc;
        this.shouldToggleLevers = shouldToggleLevers;
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
    @Override
    public int qtXMin() {
        return this.minx;
    }

    @Override
    public int qtXMid() {
        return this.getX();
    }

    @Override
    public int qtXMax() {
        return this.maxx;
    }

    @Override
    public int qtYMin() {
        return this.minz;
    }

    @Override
    public int qtYMid() {
        return this.getY();
    }

    @Override
    public int qtYMax() {
        return this.maxz;
    }
    // end interface QTBox

    // interface Comparable
    @Override
    public int compareTo(Object o) {
      // This assumes that only a single snitch can exist at a given (x,y,z)
      // Compare centers
      // TODO: Deal with volume changes when applicable
      // 1. Test X relationship
      // 2. Test Z relationship
      // 3. Test Y relationship
      Snitch other = (Snitch)o;
      int tx = this.getX();
      int ty = this.getY();
      int tz = this.getZ();
      int ox = other.getX();
      int oy = other.getY();
      int oz = other.getZ();
      if (tx < ox) {
        return -1;
      }
      if (tx > ox) {
        return 1;
      }
      if (tz < oz) {
        return -1;
      }
      if (tz > oz) {
        return 1;
      }
      if (ty < oy) {
        return -1;
      }
      if (ty > oy) {
        return 1;
      }
      return 0;  // equal
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

    public Group getGroup() {
        if (!group.isValid()) {
            group = NameAPI.getGroupManager().getGroup(group.getName());
        }
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
        JukeAlert.getInstance().getJaLogger().updateSnitchGroup(this, group.getName());
    }

    public Location getLoc() {
        return location;
    }

    public void setLoc(Location loc) {
        this.location = loc;
        calculateDimensions();
    }

    public boolean shouldLog() {
        return shouldLog;
    }

    public void setShouldLog(boolean shouldLog) {
        this.shouldLog = shouldLog;
    }
    
    public boolean shouldToggleLevers() {
    	return shouldToggleLevers;
    }
    
    public void setShouldToggleLevers(boolean shouldToggleLevers) {
    	this.shouldToggleLevers = shouldToggleLevers;
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
        return this.location.getWorld() == loc.getWorld()
                && this.location.getBlockX() == loc.getBlockX()
                && this.location.getBlockY() == loc.getBlockY()
                && this.location.getBlockZ() == loc.getBlockZ();
    }

    public void imposeSnitchTax() {
        ConfigManager config = JukeAlert.getInstance().getConfigManager();
        if (config.getTaxReinforcementPerAlert()) {
            Utility.maybeReinforcementDamaged(location.getBlock());
        }
    }
}
