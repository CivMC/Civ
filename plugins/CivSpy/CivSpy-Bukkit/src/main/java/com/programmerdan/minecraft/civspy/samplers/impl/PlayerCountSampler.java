package com.programmerdan.minecraft.civspy.samplers.impl;

import java.util.logging.Logger;

import org.bukkit.Bukkit;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PeriodicDataSample;
import com.programmerdan.minecraft.civspy.samplers.ServerSingleDataSampler;

/**
 * A simple PlayerCountSampler that periodically records the server's player count.
 * 
 * @author ProgrammerDan
 */
public final class PlayerCountSampler extends ServerSingleDataSampler {
	
	public PlayerCountSampler(DataManager manager, Logger logger, String server) {
		super(manager, logger, server);
	}

	/**
	 * Generates: <code>server.playercount</code> stat_key data.
	 */
	public DataSample sample() {
		DataSample ds = new PeriodicDataSample("server.playercount", this.getServer(),
				null, null, Bukkit.getOnlinePlayers().size());
		return ds;
	}

}
