package vg.civcraft.mc.namelayer;

import org.bukkit.configuration.file.FileConfiguration;

public class NameLayerConfigManager {

	private static FileConfiguration config;
	public void setConfigOptions(FileConfiguration config){
		NameLayerConfigManager.config = config;
	}
	
	public static String getMySQLHostName(){
		return config.getString("sql.hostname");
	}
	
	public static int getMySQLPort(){
		return config.getInt("sql.port");
	}
	
	public static String getMySQLDbname(){
		return config.getString("sql.dbname");
	}
	
	public static String getMySQLUsername(){
		return config.getString("sql.username");
	}
	
	public static String getMySQLPassword(){
		return config.getString("sql.password");
	}
	
	public static boolean getShouldLoadGroups(){
		return config.getBoolean("groups.enable", false);
	}
}
