package vg.civcraft.mc.namelayer.bungee;

import net.md_5.bungee.config.Configuration;

public class ConfigManager {

	private static Configuration config;
	
	public ConfigManager(Configuration c) {
		config = c;
	}
	
	public static String getUser() {
		return config.getString("mysql.user");
	}
	
	public static String getPassword() {
		return config.getString("mysql.password");
	}
	
	public static String getDB() {
		return config.getString("mysql.db");
	}
	
	public static String getHost() {
		return config.getString("mysql.host");
	}
	
	public static int getPort() {
		return config.getInt("mysql.port");
	}
}
