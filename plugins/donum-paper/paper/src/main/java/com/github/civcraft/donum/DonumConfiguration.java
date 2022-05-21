package com.github.civcraft.donum;

import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.configuration.file.FileConfiguration;

public class DonumConfiguration {
	
	private String complaintURL;
	
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
		this.complaintURL = config.getString("complaintURL","");
		this.host= config.getString("database.host", "localhost");
		this.database = config.getString("database.database", "global");
		this.port = config.getInt("database.port",3306);
		this.user = config.getString("database.user", "global");
		this.password = config.getString("database.password", RandomStringUtils.random(16));		
	}
	
	/**
	 * @return URL to open if players click the complain button in the GUI
	 */
	public String getComplaintURL() {
		return complaintURL;
	}
	
	/**
	 * @return Host of the database connection used
	 */
	public String getHost() {
		return host;
	}
	
	/**
	 * @return Name of the database used
	 */
	public String getDatabaseName() {
		return database;
	}
	
	/**
	 * @return Port used for the database connection
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * @return Username used for the database connection
	 */
	public String getUser() {
		return user;
	}
	
	/**
	 * @return Password used to log into the database
	 */
	public String getPassword() {
		return password;
	}
}
