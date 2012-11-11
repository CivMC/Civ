package com.untamedears.citadel.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/19/12
 * Time: 12:50 AM
 */

@Embeddable
public class ReinforcementKey implements Serializable, Comparable<ReinforcementKey> {
    private static final long serialVersionUID = 8057222586259248268L;

    private int x;
    private int y;
    private int z;
    private String world;

    public ReinforcementKey() {
    }

    public ReinforcementKey(Block block) {
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
        this.world = block.getWorld().getName();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public String getChunkId() {
        // See CitadelDao.MakeChunkId
        int chunkX;
        if (this.x < 0) {
            chunkX = (this.x - 15) / 16;
        } else {
            chunkX = this.x / 16;
        }
        int chunkZ;
        if (this.z < 0) {
            chunkZ = (this.z - 15) / 16;
        } else {
            chunkZ = this.z / 16;
        }
        return String.format(
            "%s:%d:%d", this.world, chunkX, chunkZ);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReinforcementKey)) return false;

        ReinforcementKey key = (ReinforcementKey) o;

        return x == key.x && y == key.y && z == key.z && world.equals(key.world);
    }
    
    /**
     * Order keys in lexographic order.
     * 
     * @param rk2
     * @return
     */
    public int compareTo(ReinforcementKey rk2) {
    	ReinforcementKey rk1 = this;
    	
    	if( rk1.x < rk2.x ){
    		return -1;
    	}else if( rk1.x > rk2.x ){
    		return 1;
    	}else if( rk1.y < rk2.y ){
    		return -1;
    	}else if( rk1.y > rk2.y ){
    		return 1;
    	}else if( rk1.z < rk2.z ){
    		return -1;
    	}else if( rk1.z > rk2.z ){
    		return 1;
    	}else{
    		return rk1.world.compareTo(rk2.world);
    	}
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        result = 31 * result + world.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("x: %d, y: %d, z: %d, world: %s", x, y, z, world);
    }
}
