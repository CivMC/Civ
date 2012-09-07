package com.untamedears.citadel.entity;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/21/12
 * Time: 12:01 AM
 */
public class ReinforcementMaterial implements Comparable<ReinforcementMaterial> {

    public static final HashMap<String, ReinforcementMaterial> VALID = new HashMap<String, ReinforcementMaterial>();
    public static void put(ReinforcementMaterial material) {
        VALID.put(material.getMaterial().name(), material);
    }
    public static ReinforcementMaterial get(String name) {
        Material material = Material.matchMaterial(name);
        if (material == null) {
            return null;
        } else {
            return get(material);
        }
    }
    public static ReinforcementMaterial get(Material material) {
        return VALID.get(material.name());
    }
    
    private int materialId;
    private int strength;
    private int requirements;
    private int flasherMaterialId;
    
    public ReinforcementMaterial(LinkedHashMap map) {
        // Materials may be specified by name or by integer value
    	Material material;
    	
    	material = Material.matchMaterial(map.get("name").toString());
        if (material != null) {
        	materialId = material.getId();
        } else {
        	try {
        		materialId = Integer.parseInt(map.get("name").toString()); // Non-existent (Forge) materials
        	} catch(NumberFormatException e) {
        		throw new IllegalArgumentException("Invalid reinforcement material.");
        	}
    	}
    	
        
        strength = (Integer) map.get("strength");
        requirements = (Integer) map.get("requirements");
        
        
        material = Material.matchMaterial(map.get("flasher").toString());
        if (material != null) {
        	if(!material.isBlock()) {
        		throw new IllegalArgumentException("Supplied flasher value is a non-block material.");
        	}
        	flasherMaterialId = material.getId();
        } else {
        	try {
        		flasherMaterialId = Integer.parseInt((map.get("flasher").toString()));
        	} catch(NumberFormatException e) {
        		throw new IllegalArgumentException("Invalid flasher material.");
        	}
    	}
    }

    public ReinforcementMaterial(int materialId, int strength, int requirements, int flasherMaterialId) {
        this.materialId = materialId;
        this.strength = strength;
        this.requirements = requirements;
        this.flasherMaterialId = flasherMaterialId;
    }

    public Material getMaterial() {
        return Material.getMaterial(materialId);
    }

    public int getMaterialId() {
        return materialId;
    }

    public int getStrength() {
        return strength;
    }

    public int getRequirements() {
        return requirements;
    }

    public ItemStack getRequiredMaterials() {
        return new ItemStack(getMaterial(), requirements);
    }
    
    public MaterialData getFlasher() {
        return new MaterialData(materialId);
    }

    public int compareTo(ReinforcementMaterial reinforcementMaterial) {
        return Integer.valueOf(strength).compareTo(reinforcementMaterial.getStrength());
    }

    @Override
    public String toString() {
        return String.format("name: %s, materialId: %d, strength: %d, requirements: %d", getMaterial().name(), materialId, strength, requirements);
    }
}
