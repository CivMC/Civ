package com.programmerdan.minecraft.civspy.samplers;

import java.util.logging.Logger;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.SingleDataSampler;

/**
 * Abstract wrapper expecting server name at construction.
 * 
 * Chances are good you should use this.
 * 
 * @author ProgrammerDan
 */
public abstract class ServerSingleDataSampler extends SingleDataSampler {

	private final String server;

	public ServerSingleDataSampler(DataManager target, Logger logger, String server) {
		super(target, logger);
		this.server = server;
	}
	
	public String getServer() {
		return this.server;
	}
}
