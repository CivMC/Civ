package vg.civcraft.mc.civchat2.utility;

import org.bukkit.configuration.file.FileConfiguration;

public class CivChat2Config {
	private static FileConfiguration config;
	
	public void setConfigOptions(FileConfiguration config){
		CivChat2Config.config = config;
	}
	
	public boolean getGroupsEnabled(){
		return config.getBoolean("info.groups");
	}
	
	public boolean getDebug(){
		return config.getBoolean("info.debug");
	}
	
}
