package com.programmerdan.minecraft.civspy.listeners;

import java.util.logging.Logger;

import com.programmerdan.minecraft.civspy.DataListener;
import com.programmerdan.minecraft.civspy.DataManager;

/**
 * Convenience wrapper of DataListener that embeds Server name handling.
 * 
 * @author ProgrammerDan
 */

public abstract class ServerDataListener extends DataListener {

	private final String server;
	
	public ServerDataListener(DataManager target, Logger logger, String server) {
		super(target, logger);
		this.server = server;
	}
	
	public String getServer() {
		return server;
	}

}
