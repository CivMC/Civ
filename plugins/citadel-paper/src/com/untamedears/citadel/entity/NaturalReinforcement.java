package com.untamedears.citadel.entity;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import com.untamedears.citadel.NaturalReinforcementConfig;

/**
 * User: erocs
 */
public class NaturalReinforcement implements IReinforcement {
    // HashMap<materialId, breakCount>
    public static final HashMap<Integer, NaturalReinforcementConfig> CONFIGURATION =
        new HashMap<Integer, NaturalReinforcementConfig>();

    private ReinforcementKey id_;
    private int durability_;
    private int max_durability_;

    public NaturalReinforcement() {}

    public NaturalReinforcement(Block block, int breakCount) {
        this.id_ = new ReinforcementKey(block);
        this.durability_ = breakCount;
        this.max_durability_ = breakCount;
    }

    public ReinforcementKey getId() { return id_; }
    public void setId(ReinforcementKey id) { this.id_ = id; }

    public Block getBlock() {
        try {
    	    return Bukkit.getServer().getWorld(id_.getWorld()).getBlockAt(
                    id_.getX(),
                    id_.getY(),
                    id_.getZ());
        } catch (NullPointerException e){
    	    return null;
        }
    }

    public int getDurability() { return durability_; }
    public void setDurability(int durability) { durability_ = durability; }

    public int getMaxDurability() { return max_durability_; }
    public void setMaxDurability(int max_durability) { this.max_durability_ = max_durability; }

    public double getHealth() {
        return (double) durability_ / (double) max_durability_;
    }

    public String getHealthText() {
        double health = getHealth();
        if (health > 0.75) {
            return "excellently";
        } else if (health > 0.50) {
            return "well";
        } else if (health > 0.25) {
            return "decently";
        } else {
            return "poorly";
        }
    }

    public String getStatus() { return getHealthText(); }

    public int getMaturationTime() {
        return 0;
    }

    public void setMaturationTime(int time) {
        return;
    }

    @Override
    public String toString() {
        return String.format("%s, durability: %d of %d", id_, durability_, max_durability_);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IReinforcement)) return false;

        IReinforcement r = (IReinforcement)o;
        return this.id_.equals(r.getId());
    }

    public int compareTo(IReinforcement r) {
    	return this.id_.compareTo(r.getId());
    }

    @Override
    public int hashCode() {
        return this.id_.hashCode();
    }
}
