package com.untamedears.citadel;

import java.util.LinkedHashMap;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.untamedears.citadel.NaturalReinforcementConfig;
import com.untamedears.citadel.entity.NaturalReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;
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
	private boolean reinforcedCrops;

	public void load(){
		Citadel.getPlugin().reloadConfig();
		FileConfiguration config = Citadel.getPlugin().getConfig();
		config.options().copyDefaults(true);
        flashLength = config.getInt("general.flashLength");
        autoModeReset = config.getInt("general.autoModeReset");
        verboseLogging = config.getBoolean("general.verboseLogging");
        redstoneDistance = config.getDouble("general.redstoneDistance");
        groupsAllowed = config.getInt("general.groupsAllowed");
        reinforcedCrops = config.getBoolean("general.reinforcedCrops", true);
        cacheMaxAge = config.getLong("caching.max_age");
        cacheMaxChunks = config.getInt("caching.max_chunks");
        for (Object obj : config.getList("materials")) {
            LinkedHashMap map = (LinkedHashMap) obj;
            ReinforcementMaterial.put(new ReinforcementMaterial(map));
        }
        for (String name : config.getStringList("additionalSecurable")) {
            Material material = Material.matchMaterial(name);
            if (material != null) {
            	PlayerReinforcement.SECURABLE.add(material.getId());
            } else {
            	try {
            		PlayerReinforcement.SECURABLE.add(Integer.parseInt(name));
            	} catch (NumberFormatException e) {
            		Citadel.warning("Invalid additionalSecurable material " + name);
            	}
            }
        }
        for (String name : config.getStringList("nonReinforceable")) {
        	Material material = Material.matchMaterial(name);
            if (material != null) {
            	PlayerReinforcement.NON_REINFORCEABLE.add(material.getId());
            } else {
            	try {
            		PlayerReinforcement.NON_REINFORCEABLE.add(Integer.parseInt(name));
            	} catch (NumberFormatException e) {
            		Citadel.warning("Invalid nonReinforceable material " + name);
            	}
            }
        }
        ConfigurationSection naturalReinforcements =
            config.getConfigurationSection("naturalReinforcements");
        if (naturalReinforcements != null) {
            for (String materialName : naturalReinforcements.getKeys(false)) {
                ConfigurationSection materialConfig =
                    naturalReinforcements.getConfigurationSection(materialName);
                if (materialConfig == null) {
                    Citadel.warning("Misconfigured Natural Reinforcement: " + materialName);
                    continue;
                }
                NaturalReinforcementConfig natReinCfg = new NaturalReinforcementConfig(materialConfig);
                NaturalReinforcement.CONFIGURATION.put(natReinCfg.getMaterialId(), natReinCfg);
            }
        }
	}
	
	public double getRedstoneDistance(){
		return this.redstoneDistance;
	}
	
	public void setRedstoneDistance(double rd){
		this.redstoneDistance = rd;
	}
	
	public int getAutoModeReset(){
		return this.autoModeReset;
	}
	
	public void setAutoModeReset(int amr){
		this.autoModeReset = amr;
	}
	
	public int getFlashLength(){
		return this.flashLength;
	}
	
	public void setFlashLength(int fl){
		this.flashLength = fl;
	}
	
	public int getGroupsAllowed(){
		return this.groupsAllowed;
	}

	public boolean allowReinforcedCrops() {
		return reinforcedCrops;
	}

	public void setGroupsAllowed(int ga){
		this.groupsAllowed = ga;
	}
	
	public boolean getVerboseLogging(){
		return this.verboseLogging;
	}
	
	public void setVerboseLogging(boolean vl){
		this.verboseLogging = vl;
	}
	
	public long getCacheMaxAge(){
		return this.cacheMaxAge;
	}
	
	public void setCacheMaxAge(long cma){
		this.cacheMaxAge = cma;
	}
	
	public int getCacheMaxChunks(){
		return this.cacheMaxChunks;
	}

	public void setCacheMaxChunks(int cmc){
		this.cacheMaxChunks = cmc;
	}

    public int getMaterialBreakCount(int materialId, int blockY){
        NaturalReinforcementConfig natReinCfg =
            NaturalReinforcement.CONFIGURATION.get(materialId);
        if (natReinCfg == null) {
            return 1;
        }
        return natReinCfg.generateDurability(blockY);
    }
}
