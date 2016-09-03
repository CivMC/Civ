package com.programmerdan.minecraft.civspy.listeners;

import java.util.logging.Logger;

import com.programmerdan.minecraft.civspy.DataListener;
import com.programmerdan.minecraft.civspy.DataManager;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Convenience wrapper of DataListener that embeds Server name handling. Optionally adds configuration section handling.
 *
 * Most implementations should use this as their superclass.
 *
 * @see com.programmerdan.minecraft.civspy.listeners for details on how to use this
 * 
 * @author ProgrammerDan
 */

public abstract class ServerDataListener extends DataListener {

	private final String server;
	private final ConfigurationSection config;
	
	public ServerDataListener(DataManager target, Logger logger, String server) {
		super(target, logger);
		this.server = server;
		this.config = null;
	}

	public ServerDataListener(DataManager target, Logger logger, String server, ConfigurationSection config) {
		super(target, logger);
		this.server = server;
		this.config = config;
	}
	
	public String getServer() {
		return this.server;
	}

	/**
	 * Get the configuration passed in (if any). Ideally this is parsed during startup, but
	 * it can be used directly during runtime as well.
	 * 
	 * @return the {@link org.bukkit.configuration.ConfigurationSection} for this ServerDataListener
	 */
	public ConfigurationSection getConfig() {
		return this.config;
	}

}
