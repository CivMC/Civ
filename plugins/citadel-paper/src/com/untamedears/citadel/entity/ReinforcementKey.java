package com.untamedears.citadel.entity;

import org.bukkit.block.Block;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/19/12
 * Time: 12:50 AM
 */

@Embeddable
public class ReinforcementKey implements Serializable {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReinforcementKey)) return false;

        ReinforcementKey key = (ReinforcementKey) o;

        return x == key.x && y == key.y && z == key.z && world.equals(key.world);
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
