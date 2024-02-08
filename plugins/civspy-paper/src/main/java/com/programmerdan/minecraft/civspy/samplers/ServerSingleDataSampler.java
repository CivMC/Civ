package com.programmerdan.minecraft.civspy.samplers;

import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.SingleDataSampler;

/**
 * Abstract wrapper expecting server name at construction.
 * 
 * Chances are good you should use this.
 * 
 * @see com.programmerdan.minecraft.civspy.samplers for details on how.
 * 
 * @author ProgrammerDan
 */
public abstract class ServerSingleDataSampler extends SingleDataSampler {

	private final String server;
	private final ConfigurationSection config;

	public ServerSingleDataSampler(DataManager target, Logger logger, String server) {
		super(target, logger);
		this.server = server;
		this.config = null;
	}

	public ServerSingleDataSampler(DataManager target, Logger logger, String server, ConfigurationSection config) {
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
	 * @return the {@link org.bukkit.configuration.ConfigurationSection} for this ServerSingleDataSampler
	 */
	public ConfigurationSection getConfig() {
		return this.config;
	}
}
