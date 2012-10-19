package com.untamedears.citadel.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.entity.Player;
import org.bukkit.material.Openable;

import com.untamedears.citadel.SecurityLevel;

/**
 * User: chrisrico
 */
@Entity
public class Reinforcement implements Comparable<Reinforcement> {

    public static final List<Integer> SECURABLE = new ArrayList<Integer>();
    public static final List<Integer> NON_REINFORCEABLE = new ArrayList<Integer>();
    // HashMap<materialId, breakCount>
    public static final HashMap<Integer, Integer> HARDENED_BREAK_COUNTS = new HashMap<Integer, Integer>();

    @Id private ReinforcementKey id;

    private Integer materialId;
    private int durability;
    private SecurityLevel securityLevel;

    @ManyToOne
    @JoinColumn(name = "name")
    private Faction owner;

    public Reinforcement() {
    }

    public Reinforcement(Block block, ReinforcementMaterial material, Faction owner, SecurityLevel securityLevel) {
        this.id = new ReinforcementKey(block);
        this.materialId = material.getMaterial().getId();
        this.durability = material.getStrength();
        this.owner = owner;
        this.securityLevel = securityLevel;
    }

    public Reinforcement(Block block, int breakCount) {
        this.id = new ReinforcementKey(block);
        this.materialId = null;
        this.durability = breakCount;
        this.owner = null;
        this.securityLevel = SecurityLevel.GENERATED;
    }

    public ReinforcementKey getId() {
        return id;
    }

    public void setId(ReinforcementKey id) {
        this.id = id;
    }

    public Block getBlock() {
        Block block;
        try {
        	block = Bukkit.getServer().getWorld(id.getWorld()).getBlockAt(id.getX(), id.getY(), id.getZ());
        } catch (NullPointerException e){
        	return null;
        }
        return block;
    }

    public ReinforcementMaterial getMaterial() {
	if (materialId == null) {
		return null;
 	}
        return ReinforcementMaterial.get(Material.getMaterial(materialId));
    }
    
    public Integer getMaterialId() {
        return materialId;
    }
    
    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public int getDurability() {
        return durability;
    }

    public void setDurability(int durability) {
        this.durability = durability;
    }
    
    public SecurityLevel getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(SecurityLevel securityLevel) {
        this.securityLevel = securityLevel;
    }

    public Faction getOwner() {
        return owner;
    }

    public void setOwner(Faction group) {
        this.owner = group;
    }

    public double getHealth() {
        return (double) durability / (double) getMaterial().getStrength();
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
    
    public String getStatus() {
        String verb;
        if (isSecurable()) {
            verb = "Locked";
        } else {
            verb = "Reinforced";
        }
        return String.format("%s %s with %s",
                verb,
                getHealthText(),
                getMaterial().getMaterial().name());
    }
    
    public boolean isAccessible(Player player) {
        String name = player.getDisplayName();
        return isAccessible(name);
    }
    
    public boolean isAccessible(String name) {
        switch (securityLevel) {
            case PRIVATE:
                return name.equals(owner.getFounder());
            case GROUP:
                return name.equals(owner.getFounder()) || owner.isMember(name) || owner.isModerator(name);
            case PUBLIC:
            	return true;
	    case GENERATED:
		return false;
        }
        return false;
    }
    
    public boolean isBypassable(Player player) {
        String name = player.getDisplayName();
        switch (securityLevel) {
            case PRIVATE:
                return name.equals(owner.getFounder());
	    case GENERATED:
		return false;
            default:
                return name.equals(owner.getFounder()) || owner.isModerator(name);
        }
    }

    public boolean isSecurable() {
        Block block = getBlock();
        return block.getState() instanceof ContainerBlock
                || block.getState().getData() instanceof Openable
                || SECURABLE.contains(block.getTypeId());
    }

    public Reinforcement clone(Block block) {
        Reinforcement clone = new Reinforcement(block, getMaterial(), getOwner(), securityLevel);
        clone.setDurability(durability);
        return clone;
    }

    @Override
    public String toString() {
        return String.format("%s, material: %s, durability: %d", id, getMaterial().getMaterial().name(), durability);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reinforcement)) return false;

        Reinforcement rein = (Reinforcement) o;

        return this.id.equals(rein.id);
    }
    
    public int compareTo( Reinforcement r ){
    	return this.id.compareTo( r.id );
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
