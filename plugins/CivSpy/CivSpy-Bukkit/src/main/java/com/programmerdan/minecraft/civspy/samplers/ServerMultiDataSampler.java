package com.programmerdan.minecraft.civspy.samplers;

import java.util.logging.Logger;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.MultiDataSampler;

/**
 * Abstract wrapper expecting server name at construction.
 * 
 * Chances are good you should use this.
 * 
 * @author ProgrammerDan
 */
public abstract class ServerMultiDataSampler extends MultiDataSampler {

	private final String server;

	public ServerMultiDataSampler(DataManager target, Logger logger, String server) {
		super(target, logger);
		this.server = server;
	}
	
	public String getServer() {
		return this.server;
	}

}