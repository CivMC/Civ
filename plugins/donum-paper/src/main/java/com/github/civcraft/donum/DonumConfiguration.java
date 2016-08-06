package com.github.civcraft.donum;

import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class DonumConfiguration {
	
	private String complaintURL;
	private boolean isMercuryEnabled;
	
	private String host;
	private String database;
	private int port;
	private String user;
	private String password;
	
	
	public void parse() {
		Donum plugin = Donum.getInstance();
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		FileConfiguration config = plugin.getConfig();
		this.isMercuryEnabled = Bukkit.getPluginManager().isPluginEnabled("Mercury");
		this.complaintURL = config.getString("complaintURL","");
		this.host= config.getString("database.host", "localhost");
		this.database = config.getString("database.database", "global");
		this.port = config.getInt("database.port",3306);
		this.user = config.getString("database.user", "global");
		this.password = config.getString("database.password", RandomStringUtils.random(16));		
	}
	
	public String getComplaintURL() {
		return complaintURL;
	}
	
	public boolean isMercuryEnabled() {
		return isMercuryEnabled;
	}
	
	public String getHost() {
		return host;
	}
	
	public String getDatabaseName() {
		return database;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getPassword() {
		return password;
	}
}
