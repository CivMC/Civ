package com.programmerdan.minecraft.civspy.samplers;

import java.util.logging.Logger;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.MultiDataSampler;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Abstract wrapper expecting server name at construction. Optionally, configuration section.
 * 
 * Chances are good you should use this.
 *
 * @see com.programmerdan.minecraft.civspy.samplers for details on how.
 * 
 * @author ProgrammerDan
 */
public abstract class ServerMultiDataSampler extends MultiDataSampler {

	private final String server;
	private final ConfigurationSection config;

	public ServerMultiDataSampler(DataManager target, Logger logger, String server) {
		super(target, logger);
		this.server = server;
		this.config = null;
	}

	public ServerMultiDataSampler(DataManager target, Logger logger, String server, ConfigurationSection config) {
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
	 * @return the {@link org.bukkit.configuration.ConfigurationSection} for this ServerMultiDataSampler
	 */
	public ConfigurationSection getConfig() {
		return this.config;
	}

}
