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
	private long cacheMaxAge;
	private int cacheMaxChunks;

	public void load(){
		Citadel.getPlugin().reloadConfig();
		FileConfiguration config = Citadel.getPlugin().getConfig();
		config.options().copyDefaults(true);
        flashLength = config.getInt("general.flashLength");
        autoModeReset = config.getInt("general.autoModeReset");
        verboseLogging = config.getBoolean("general.verboseLogging");
        redstoneDistance = config.getDouble("general.redstoneDistance");
        groupsAllowed = config.getInt("general.groupsAllowed");
        cacheMaxAge = config.getLong("caching.max_age");
        cacheMaxChunks = config.getInt("caching.max_chunks");
        for (Object obj : config.getList("materials")) {
            LinkedHashMap map = (LinkedHashMap) obj;
            ReinforcementMaterial.put(new ReinforcementMaterial(map));
        }
        for (String name : config.getStringList("additionalSecurable")) {
            Material material = Material.matchMaterial(name);
            if (material == null) {
                Citadel.warning("Invalid additionalSecurable material " + name);
            } else {
                Reinforcement.SECURABLE.add(material.getId());
            }
        }
        for (String name : config.getStringList("nonReinforceable")) {
            Material material = Material.matchMaterial(name);
            if (material == null) {
                Citadel.warning("Invalid nonReinforceable material " + name);
            } else {
                Reinforcement.NON_REINFORCEABLE.add(material.getId());
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
	
	public long getCacheMaxAge(){
		return this.cacheMaxAge;
	}
	
	public int getCacheMaxChunks(){
		return this.cacheMaxChunks;
	}
}
