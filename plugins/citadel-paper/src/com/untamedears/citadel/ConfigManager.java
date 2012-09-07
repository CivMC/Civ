package com.untamedears.citadel;

import java.util.LinkedHashMap;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import com.untamedears.citadel.entity.Reinforcement;
import com.untamedears.citadel.entity.ReinforcementMaterial;

/**
 * User: JonnyD
 * Date: 07/18/12
 * Time: 11:57 PM
 */
public class ConfigManager {
	
	private int flashLength;
	private int autoModeReset;
	private boolean verboseLogging;
	private double redstoneDistance;
	private int groupsAllowed;

	public void load(){
		Citadel.getPlugin().reloadConfig();
		FileConfiguration config = Citadel.getPlugin().getConfig();
		config.options().copyDefaults(true);
        flashLength = config.getInt("general.flashLength");
        autoModeReset = config.getInt("general.autoModeReset");
        verboseLogging = config.getBoolean("general.verboseLogging");
        redstoneDistance = config.getDouble("general.redstoneDistance");
        groupsAllowed = config.getInt("general.groupsAllowed");
        for (Object obj : config.getList("materials")) {
            LinkedHashMap map = (LinkedHashMap) obj;
            ReinforcementMaterial.put(new ReinforcementMaterial(map));
        }
        for (String name : config.getStringList("additionalSecurable")) {
            Material material = Material.matchMaterial(name);
            if (material != null) {
            	Reinforcement.SECURABLE.add(material.getId());
            } else {
            	try {
            		Reinforcement.SECURABLE.add(Integer.parseInt(name));
            	} catch (NumberFormatException e) {
            		Citadel.warning("Invalid additionalSecurable material " + name);
            	}
            }
        }
        for (String name : config.getStringList("nonReinforceable")) {
        	Material material = Material.matchMaterial(name);
            if (material != null) {
            	Reinforcement.NON_REINFORCEABLE.add(material.getId());
            } else {
            	try {
            		Reinforcement.NON_REINFORCEABLE.add(Integer.parseInt(name));
            	} catch (NumberFormatException e) {
            		Citadel.warning("Invalid nonReinforceable material " + name);
            	}
            }
        }
	}
	
	public double getRedstoneDistance(){
		return this.redstoneDistance;
	}
	
	public int getAutoModeReset(){
		return this.autoModeReset;
	}
	
	public int getFlashLength(){
		return this.flashLength;
	}
	
	public int getGroupsAllowed(){
		return this.groupsAllowed;
	}
	
	public boolean getVerboseLogging(){
		return this.verboseLogging;
	}
}
